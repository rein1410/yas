package com.yas.order.service;

import static com.yas.order.utils.Constants.ErrorCode.CHECKOUT_NOT_FOUND;

import com.yas.order.exception.Forbidden;
import com.yas.order.exception.NotFoundException;
import com.yas.order.model.Checkout;
import com.yas.order.model.CheckoutItem;
import com.yas.order.model.Order;
import com.yas.order.model.enumeration.CheckoutState;
import com.yas.order.repository.CheckoutItemRepository;
import com.yas.order.repository.CheckoutRepository;
import com.yas.order.utils.AuthenticationUtils;
import com.yas.order.utils.Constants;
import com.yas.order.viewmodel.checkout.CheckoutPostVm;
import com.yas.order.viewmodel.checkout.CheckoutStatusPutVm;
import com.yas.order.viewmodel.checkout.CheckoutVm;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CheckoutService {
    private final CheckoutRepository checkoutRepository;
    private final CheckoutItemRepository checkoutItemRepository;
    private final OrderService orderService;

    public CheckoutVm createCheckout(CheckoutPostVm checkoutPostVm) {
        UUID uuid = UUID.randomUUID();
        Checkout checkout = Checkout.builder()
                .id(uuid.toString())
                .email(checkoutPostVm.email())
                .note(checkoutPostVm.note())
                .couponCode(checkoutPostVm.couponCode())
                .checkoutState(CheckoutState.PENDING)
                .build();
        checkoutRepository.save(checkout);

        Set<CheckoutItem> checkoutItems = checkoutPostVm.checkoutItemPostVms().stream()
                .map(item -> CheckoutItem.builder()
                        .productId(item.productId())
                        .productName(item.productName())
                        .quantity(item.quantity())
                        .productPrice(item.productPrice())
                        .note(item.note())
                        .discountAmount(item.discountAmount())
                        .taxPercent(item.taxPercent())
                        .taxAmount(item.taxAmount())
                        .checkoutId(checkout)
                        .build())
                .collect(Collectors.toSet());
        checkoutItemRepository.saveAll(checkoutItems);

        //setCheckoutItem so that we able to return checkout with checkoutItems
        checkout.setCheckoutItem(checkoutItems);
        return CheckoutVm.fromModel(checkout);
    }

    public CheckoutVm getCheckoutPendingStateWithItemsById(String id) {
        Checkout checkout = checkoutRepository.findByIdAndCheckoutState(id, CheckoutState.PENDING).orElseThrow(()
                -> new NotFoundException(CHECKOUT_NOT_FOUND, id));

        if (!checkout.getCreatedBy().equals(AuthenticationUtils.getCurrentUserId())) {
            throw new Forbidden(Constants.ErrorCode.FORBIDDEN);
        }
        return CheckoutVm.fromModel(checkout);
    }

    public Long updateCheckoutStatus(CheckoutStatusPutVm checkoutStatusPutVm) {
        Checkout checkout = checkoutRepository.findById(checkoutStatusPutVm.checkoutId())
                .orElseThrow(() -> new NotFoundException(CHECKOUT_NOT_FOUND, checkoutStatusPutVm.checkoutId()));
        checkout.setCheckoutState(CheckoutState.valueOf(checkoutStatusPutVm.checkoutStatus()));
        checkoutRepository.save(checkout);
        Order order = orderService.findOrderByCheckoutId(checkoutStatusPutVm.checkoutId());
        return order.getId();
    }
}
