package com.fernando.microservices.cart_service.services;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fernando.microservices.cart_service.config.OpenFeignConfig;
import com.fernando.microservices.cart_service.config.OpenFeignShippingRule;
import com.fernando.microservices.cart_service.config.OpenFeignUserInfo;
import com.fernando.microservices.cart_service.dto.CompanyDto;
import com.fernando.microservices.cart_service.dto.ProductDto;
import com.fernando.microservices.cart_service.dto.ProductView;
import com.fernando.microservices.cart_service.dto.ShippingRuleDto;
import com.fernando.microservices.cart_service.dto.UserInfoDto;
import com.fernando.microservices.cart_service.entity.Cart;
import com.fernando.microservices.cart_service.entity.CartItem;
import com.fernando.microservices.cart_service.entity.CartItemStatus;
import com.fernando.microservices.cart_service.entity.CartStatus;
import com.fernando.microservices.cart_service.repositories.CartItemRepository;
import com.fernando.microservices.cart_service.repositories.CartRepository;
import com.fernando.microservices.common_service.events.order_events.CreateOrderEvent;
import com.fernando.microservices.common_service.events.order_events.Item;
import com.fernando.microservices.common_service.events.order_events.OrderConfirmedEvent;
import com.fernando.microservices.common_service.events.order_events.OrderNotCreatedEvent;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OpenFeignConfig feignConfig;
    private final JmsService jmsService;
    private final OpenFeignShippingRule feignShippingRule;
    private final OpenFeignUserInfo feignUserInfo;

    @Override
    @Transactional
    public void addItemToCart(Long userId, HttpServletRequest request, HttpServletResponse response, String productId,
            Long ruleId) {
        Cart cart;

        if (userId != null) {
            cart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setId(UUID.randomUUID().toString());
                        newCart.setUserId(userId);
                        newCart.setCreatedAt(LocalDateTime.now());
                        newCart.setUpdatedAt(LocalDateTime.now());
                        newCart.setStatus(CartStatus.ACTIVE);
                        cartRepository.save(newCart);

                        return newCart;
                    });
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                String generatedToken = createResponseCookie(response);
                Cart createdCart = new Cart();
                createdCart.setAnonymousToken(generatedToken);
                createdCart.setId(UUID.randomUUID().toString());
                createdCart.setCreatedAt(LocalDateTime.now());
                createdCart.setUpdatedAt(LocalDateTime.now());
                createdCart.setStatus(CartStatus.ACTIVE);
                cart = cartRepository.save(createdCart);
            } else {
                String token = Arrays.stream(cookies)
                        .filter(c -> c.getName().equals("get.sde-wea-sdewqqas"))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElseGet(() -> {
                            String generatedToken = createResponseCookie(response);
                            Cart createdCart = new Cart();
                            createdCart.setAnonymousToken(generatedToken);
                            createdCart.setId(UUID.randomUUID().toString());
                            createdCart.setCreatedAt(LocalDateTime.now());
                            createdCart.setUpdatedAt(LocalDateTime.now());
                            createdCart.setStatus(CartStatus.ACTIVE);
                            cartRepository.save(createdCart);
                            return generatedToken;
                        });

                        cart = cartRepository.findByAnonymousToken(token)
                                .orElseGet(() -> {
                                    String generatedToken = createResponseCookie(response);
                                    Cart createdCart = new Cart();
                                    createdCart.setAnonymousToken(generatedToken);
                                    createdCart.setId(UUID.randomUUID().toString());
                                    createdCart.setCreatedAt(LocalDateTime.now());
                                    createdCart.setUpdatedAt(LocalDateTime.now());
                                    createdCart.setStatus(CartStatus.ACTIVE);
                                    cartRepository.save(createdCart);
                                    return createdCart;
                                });
            }

        }

        /*
         * Modify this for the update 1.0.1
         * to get the product details from the product service
         * using a message broker instead of feign client,
         * to avoid the tight coupling between the services
         * and to improve the performance of the cart service by
         * reducing the number of calls to the product service
         */
        ProductView productView = feignConfig.getProductViewById(productId);

        Optional<CartItem> cartItem = cartItemRepository.findByCartIdAndProductProductId(cart.getId(), productId);
        if (cartItem.isPresent()) {
            cartItem.get().increaseQuantity();
            cartItemRepository.save(cartItem.get());
            return;
        }

        ProductDto productDto = new ProductDto();
        productDto.setProductId(productId);
        productDto.setName(productView.getName());
        productDto.setPrice(productView.getPrice());
        productDto.setImages(productView.getImageUrls());

        CartItem newItem = new CartItem();
        newItem.setRuleId(ruleId);
        newItem.setCompanyId(productView.getCompanyId());
        newItem.setCart(cart);
        newItem.setProduct(productDto);
        newItem.setQuantity(BigInteger.ONE);
        newItem.setCart(cart);
        newItem.calculateSubtotal();
        newItem.setStatus(CartItemStatus.UNCHECKED);
        cartItemRepository.save(newItem);

        cart.getItems().add(newItem);
        calculateTotalShippingPrice(cart);
        cart.calculateTotalPrice();
        cart.setTotalCharge(cart.getTotalPrice() + cart.getTotalShippingPrice());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

    }

    @Override
    @Transactional
    public void removeItemFromCart(String cartId, String productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + cartId));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found for product id: " + productId));

        cart.getItems().remove(cartItem);
        calculateTotalShippingPrice(cart);
        cart.calculateTotalPrice();
        cart.setTotalCharge(cart.getTotalPrice() + cart.getTotalShippingPrice());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void increaseItemQuantity(String cartId, String productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + cartId));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found for product id: " + productId));

        cartItem.increaseQuantity();
        calculateTotalShippingPrice(cart);
        cart.calculateTotalPrice();
        cart.setTotalCharge(cart.getTotalPrice() + cart.getTotalShippingPrice());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void decreaseItemQuantity(String cartId, String productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + cartId));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found for product id: " + productId));

        cartItem.decreaseQuantity();
        cartItemRepository.save(cartItem);

        calculateTotalShippingPrice(cart);
        cart.calculateTotalPrice();
        cart.setTotalCharge(cart.getTotalPrice() + cart.getTotalShippingPrice());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void toggleItemCheckedStatus(String cartId, String productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + cartId));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found for product id: " + productId));

        if (cartItem.getStatus() == CartItemStatus.CHECKED) {
            cartItem.setStatus(CartItemStatus.UNCHECKED);
            cartItem.calculateSubtotal();
            cartItemRepository.save(cartItem);

            calculateTotalShippingPrice(cart);
            cart.calculateTotalPrice();
            cart.setTotalCharge(cart.getTotalPrice() + cart.getTotalShippingPrice());
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        } else {
            cartItem.setStatus(CartItemStatus.CHECKED);
            cartItem.calculateSubtotal();
            cartItemRepository.save(cartItem);

            calculateTotalShippingPrice(cart);
            cart.calculateTotalPrice();
            cart.setTotalCharge(cart.getTotalPrice() + cart.getTotalShippingPrice());
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user id: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCartByAnonymousToken(HttpServletRequest request) {
        String anonymousToken = verifyCookieExistance(request.getCookies());
        if (anonymousToken == null) {
            Cart newCart = new Cart();
            return cartRepository.save(newCart);
        }
        return cartRepository.findByAnonymousToken(anonymousToken)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setAnonymousToken(anonymousToken);
                    newCart.setId(UUID.randomUUID().toString());
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setUpdatedAt(LocalDateTime.now());
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });
    }

    // @Override
    // @Transactional
    // public String createHandshakeCart(HttpServletRequest request,
    // HttpServletResponse response) {
    // Cookie[] cookies = request.getCookies();

    // String token = Arrays.stream(cookies)
    // .filter(c -> c.getName().equals("get.sde-wea-sdewqqas"))
    // .map(Cookie::getValue)
    // .findFirst()
    // .orElseGet(() -> {
    // String generatedToken = createResponseCookie(response);
    // Cart cart = new Cart();
    // cart.setAnonymousToken(generatedToken);
    // cart.setId(UUID.randomUUID().toString());
    // cart.setCreatedAt(LocalDateTime.now());
    // cart.setUpdatedAt(LocalDateTime.now());
    // cart.setStatus(CartStatus.ACTIVE);
    // cartRepository.save(cart);
    // return generatedToken;
    // });

    // return token;
    // }

    public String verifyCookieExistance(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals("get.sde-wea-sdewqqas"))
                .map(Cookie::getValue)
                .findFirst()
                .get();
    }

    public String createResponseCookie(HttpServletResponse response) {
        String token = UUID.randomUUID().toString();
        ResponseCookie responseCookie = ResponseCookie.from("get.sde-wea-sdewqqas", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return token;
    }

    @Override
    @Transactional
    public void mergeCart(HttpServletRequest request, HttpServletResponse response, Long userId) {
        String anonymousToken = verifyCookieExistance(request.getCookies());

        if (anonymousToken == null) return;

        Cart anonymousCart = cartRepository.findByAnonymousToken(anonymousToken)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Cart userCart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setId(UUID.randomUUID().toString());
                    newCart.setUserId(userId);
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setUpdatedAt(LocalDateTime.now());
                    newCart.calculateTotalPrice();
                    newCart.setStatus(CartStatus.ACTIVE);

                    return cartRepository.save(newCart);
                });

        List<CartItem> anonymousItems = anonymousCart.getItems();
        List<CartItem> userItems = userCart.getItems();

        UserInfoDto userInfoDto = feignUserInfo.getUserInfoByUserId(userId.toString());
        String destination = userInfoDto.getCity();

        List<CartItem> toRemove = new ArrayList<>();
        for (CartItem anonyItem : anonymousItems) {
            Optional<CartItem> existing = userItems.stream()
                    .filter(i -> i.getProduct().getProductId().equals(anonyItem.getProduct().getProductId()))
                    .findFirst();

            CompanyDto companyDto = feignUserInfo.getCompanyById(anonyItem.getCompanyId());
            String origin = companyDto.getCity();

            ShippingRuleDto shippingRuleDto = feignShippingRule.getRuleByOriginAndDestination(origin, destination);
            Long ruleId = shippingRuleDto.getId();
            System.out.println("Rule id for origin: " + origin + " and destination: " + destination + " is: " + ruleId);

            if (existing.isPresent()) {
                existing.get().increaseQuantity();
                existing.get().setRuleId(ruleId);
                cartItemRepository.save(existing.get());

                cartRepository.save(userCart);
            } else {
                // anonyItem.setCart(userCart);
                // cartItemRepository.save(anonyItem);

                // userCart.getItems().add(anonyItem);
                CartItem newCartItem = new CartItem();
                newCartItem.setProduct(anonyItem.getProduct());
                newCartItem.setQuantity(anonyItem.getQuantity());
                newCartItem.setStatus(anonyItem.getStatus());
                newCartItem.setSubtotal(anonyItem.getSubtotal());
                newCartItem.setRuleId(ruleId);
                newCartItem.setCompanyId(anonyItem.getCompanyId());
                newCartItem.setCart(userCart);

                userCart.getItems().add(newCartItem);
            }
            toRemove.add(anonyItem);
        }

        calculateTotalShippingPrice(userCart);
        userCart.calculateTotalPrice();
        userCart.setTotalCharge(
                userCart.getTotalPrice() + userCart.getTotalShippingPrice());
        userCart.setUpdatedAt(LocalDateTime.now());
        userCart.setAnonymousToken(null);

        cartRepository.save(userCart);

        cartItemRepository.deleteAll(toRemove);
        cartRepository.delete(anonymousCart);

        ResponseCookie responseCookie = ResponseCookie.from("get.sde-wea-sdewqqas", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

    }

    @Override
    public void createOrderFromCart(Long userId) {
        String userIdString = userId.toString();
        UserInfoDto userInfoDto = feignUserInfo.getUserInfoByUserId(userIdString);

        if (userInfoDto == null) {
            throw new RuntimeException("User has no valid information");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User has no cart, thus can't checkout"));

        if (cart.getStatus().equals(CartStatus.CHECKED_OUT)) {
            throw new RuntimeException("Cart already checked out");
        }

        List<CartItem> checkedItems = cartItemRepository.findByStatusAndCartId(CartItemStatus.CHECKED, cart.getId());

        if (checkedItems.isEmpty()) {
            throw new RuntimeException("No checked items to create order from");
        }

        List<Item> orderItems = checkedItems.stream()
                .map(i -> {
                    Item item = new Item();
                    item.setProductId(i.getProduct().getProductId());
                    item.setCompanyId(i.getCompanyId());
                    item.setRuleId(i.getRuleId());
                    item.setName(i.getProduct().getName());
                    item.setImageUrls(i.getProduct().getImages());
                    item.setPrice(i.getProduct().getPrice());
                    item.setQuantity(i.getQuantity());
                    item.setSubtotal(i.getSubtotal());
                    return item;
                })
                .toList();

        CreateOrderEvent createOrderEvent = new CreateOrderEvent(userId, orderItems, cart.getTotalCharge());

        jmsService.sendEvent(createOrderEvent);

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);
    }

    public void calculateTotalShippingPrice(Cart cart) {
        Map<Long, Double> shippingByCompany = new HashMap<>();
        Map<Long, Double> ruleCache = new HashMap<>();

        List<CartItem> checkedItems = cart.getItems().stream()
                .filter(i -> i.getStatus().equals(CartItemStatus.CHECKED))
                .toList();

        boolean hasMissingRules = checkedItems.stream()
                .anyMatch(i -> i.getRuleId() == null);

        if (hasMissingRules) {
            cart.setTotalShippingPrice(0.0);
            return;
        }

        for (CartItem cartItem : checkedItems) {

            if (cartItem.getStatus() != CartItemStatus.CHECKED)
                continue;

            Long companyId = cartItem.getCompanyId();
            Long ruleId = cartItem.getRuleId();

            if (companyId == null || ruleId == null)
                continue;

            shippingByCompany.computeIfAbsent(companyId, c -> {
                return ruleCache.computeIfAbsent(ruleId, this::getShippingPrice);
            });
        }

        double totalShipping = shippingByCompany.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        cart.setTotalShippingPrice(totalShipping);
    }

    public Double getShippingPrice(Long ruleId) {
        ShippingRuleDto shippingRuleDto = feignShippingRule.getRuleById(ruleId);
        return shippingRuleDto.getPrice();
    }

    @Override
    @JmsListener(destination = "error.order.queue")
    public void rollbackCheckedoutCart(OrderNotCreatedEvent orderNotCreatedEvent) {
        System.out.println("Rolling back the changes, because order has errors");
        Cart cart = cartRepository.findByUserId(orderNotCreatedEvent.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "Cart not found by that userId: " + orderNotCreatedEvent.getUserId()));

        cart.setStatus(CartStatus.ACTIVE);
        cartRepository.save(cart);
    }

    @JmsListener(destination = "confirmed.order.queue")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        System.out.println("Cart deleted, because the order was confirmed");
        cartRepository.findByUserId(event.getUserId())
                .ifPresent(cart -> {
                    cartRepository.delete(cart);
                });
    }

}
