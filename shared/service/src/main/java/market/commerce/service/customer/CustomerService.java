package market.commerce.service.customer;

import lombok.RequiredArgsConstructor;
import market.commerce.exception.MessageException;
import market.commerce.exception.NotFoundException;
import market.commerce.model.customer.Address;
import market.commerce.model.customer.Customer;
import market.commerce.model.enums.Status;
import market.commerce.repository.CustomerRepository;
import market.commerce.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Tergel
 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthService authService;

    public Customer findById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("data.not-found"));
    }

    public Customer register(Customer customer, String rawPassword) {
        if (!ObjectUtils.isEmpty(customer.getEmail())
                && customerRepository.existsByEmail(customer.getEmail()))
            throw new MessageException("data.exists");

        if (!ObjectUtils.isEmpty(customer.getPhone())
                && customerRepository.existsByPhone(customer.getPhone()))
            throw new MessageException("data.exists");

        customer.setPassword(authService.encodePassword(rawPassword));
        customer.setStatus(Status.ACTIVE);
        customer.setEmailVerified(false);
        customer.setPhoneVerified(false);
        customer.setOrderCount(0L);
        return customerRepository.save(customer);
    }

    public Customer addAddress(String customerId, Address address) {
        Customer customer = findById(customerId);
        List<Address> addresses = customer.getAddresses() == null
                ? new ArrayList<>() : customer.getAddresses();

        address.setId(UUID.randomUUID().toString());

        if (Boolean.TRUE.equals(address.getIsDefault()) || addresses.isEmpty()) {
            addresses.forEach(existing -> existing.setIsDefault(false));
            address.setIsDefault(true);
            customer.setDefaultAddressId(address.getId());
        }

        addresses.add(address);
        customer.setAddresses(addresses);
        return customerRepository.save(customer);
    }

    public Customer removeAddress(String customerId, String addressId) {
        Customer customer = findById(customerId);
        if (customer.getAddresses() != null)
            customer.getAddresses().removeIf(address -> addressId.equals(address.getId()));

        if (addressId.equals(customer.getDefaultAddressId()))
            customer.setDefaultAddressId(null);

        return customerRepository.save(customer);
    }
}
