# market-backend

E-commerce backend. Gradle multi-module, Java 21, Spring Boot 3.2.12, MongoDB —
same layout and conventions as `mnpost-backend`.

## Modules

```
shared/
  common       utils with no domain knowledge (date, string, file, slug, logging aspect)
  model        documents, enums, repositories, DAOs, mongo config, i18n, exception handling
  auth         JWT issue/verify, security filter chain, @AuthUser resolver
  service      business logic (catalog, cart, order, payment, inventory, coupon, customer)
  web          WebMvcConfig + argument resolvers

aws/
  aws-common   credentials + region beans
  aws-s3       product image / file upload

integration/
  integration-payment    PaymentGateway interface + QPay implementation
  integration-delivery   DeliveryProvider interface + flat-rate in-house courier

apis/
  public-api   storefront: catalog, cart, checkout, orders, payment callback   :8080 /public-api
  manage-api   back-office: products, orders, inventory, merchants            :8081 /manage-api

cron/
  cron-shared  scheduled jobs: order expiry, payment reconcile, low stock      :8082 /cron
```

Dependency direction is one-way: `apis`/`cron` → `shared:service` → `shared:auth` →
`shared:model` → `shared:common`. Nothing in `shared` depends on an api module.

## Running locally

MongoDB must be a **replica set** — checkout runs in a transaction, and a
standalone `mongod` rejects those.

```bash
# one-time: start mongo as a single-node replica set
mongod --replSet rs0 --dbpath /usr/local/var/mongodb
mongosh --eval 'rs.initiate()'

./gradlew :apis:public-api:bootRun
./gradlew :apis:manage-api:bootRun
./gradlew :cron:cron-shared:bootRun
```

## Profiles

`application.properties` holds everything shared; the Mongo connection lives in
the profile files:

| profile      | database        |
|--------------|-----------------|
| `default`    | `market-local`  |
| `staging`    | `MONGODB_URI`   |
| `production` | `MONGODB_URI`   |

Secrets come from environment variables (`JWT_SECRET`, `AWS_ACCESS_KEY`,
`QPAY_USERNAME`, ...) — no credentials are committed. When `aws.access-key` is
empty the S3 beans are skipped, so a local run needs no AWS account.

## Sign-in

Password login (`POST /v1/auth/login`) and social login (`POST /v1/auth/social`)
both end at the same place: a Customer and a `Market-Auth` JWT. Everything
downstream — the filter, `AuthUserPrincipal`, every controller — is unaware of
which was used.

Google, Apple and phone OTP all go through **Firebase Auth**, and all three hit
the *same* endpoint: the client SDK completes the provider flow and the sign-in
method arrives as a claim inside the Firebase token.

```
POST /v1/auth/social   { "idToken": "<firebase id token>" }
```

**There is no server-side OTP endpoint.** For phone login the Firebase client
SDK sends the SMS and checks the code, including reCAPTCHA/App Check abuse
protection; the backend only ever sees the resulting token. Token verification
is local — the Admin SDK caches Google's public keys, so it is not a network
call per login.

Firebase is off by default so a local run needs no service account:

```properties
firebase.enabled=${FIREBASE_ENABLED:false}
firebase.project-id=${FIREBASE_PROJECT_ID:}
firebase.credentials-path=${FIREBASE_CREDENTIALS_PATH:}   # file: or classpath:
firebase.credentials-json=${FIREBASE_CREDENTIALS_JSON:}   # raw json, for containers
```

With it off, `/v1/auth/social` returns a localized 400 rather than failing to start.

### Account resolution

`SocialAuthService` resolves a token to a Customer in this order:

1. **By `firebaseUid`.** Firebase merges Google, Apple and phone into one uid,
   so this is the only stable key — never the email, which can change or be an
   Apple private relay address.
2. **By verified email**, linking to an existing account. Only when
   `email_verified` is true: linking on an unverified address would let someone
   pre-register a victim's email and capture their real login.
3. **By phone number**, linking. Reaching phone sign-in at all means Firebase
   verified the number.
4. Otherwise create a Customer with `password = null`.

Password login on such an account reports "signs in with Google, Apple or a
phone number" rather than "wrong password".

Swapping Firebase out later means writing one new `SocialTokenVerifier` that
returns a `SocialUserInfo`; `SocialAuthService`, the Customer model and the
controllers do not change.

## Domain notes

**Product vs variant.** `Product` is the catalog entry; `ProductVariant` is what
is actually bought and carries the SKU, price and barcode. Price range and stock
total on the product are denormalized from the variants for listing queries —
`ProductService.refreshAggregates` recomputes them.

**Stock.** `available = quantity - reserved`. Checkout reserves, payment commits,
cancel/timeout releases. Every mutation is a conditional atomic update whose
filter asserts sufficient stock, so concurrent checkouts cannot oversell without
a lock. `InventoryMovement` is an append-only ledger of the same changes.

**Orders are snapshots.** Product name, price, options and the shipping address
are copied onto the order, so editing the catalog later never rewrites history.
Allowed status transitions are declared on `OrderStatus.next()`.

**Payments.** A callback is never trusted on its own — `PaymentService` asks the
gateway to confirm before marking an order paid, and ignores repeat callbacks.
`PaymentReconcileJob` catches payments whose callback never arrived.

**Money** is `BigDecimal` stored as Decimal128 (never a double); all arithmetic
goes through `MoneyUtil`.

## What is scaffolded but not finished

- `QPayGateway` — invoice/verify/refund HTTP calls are `TODO`, token caching too
- `PaymentReconcileJob` — logs pending payments, does not settle them yet
- `LowStockAlertJob` — logs, does not notify merchants
- No notification module (email/SMS) yet
- Apple account deletion — Apple requires apps offering sign-up to offer
  deletion and to call its token-revocation endpoint; that needs the `.p8` key,
  team id and key id, none of which login itself requires
- No tests
