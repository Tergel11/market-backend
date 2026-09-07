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

Swagger UI: `http://localhost:8080/public-api/open-api-documentation`

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
- No tests
