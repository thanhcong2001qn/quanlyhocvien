package com.dacs.quanlyhocvien.Controllers.user;

import com.dacs.quanlyhocvien.Services.CartService;
import com.dacs.quanlyhocvien.models.CartItemModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final CartService cartService;

    @Autowired
    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Lấy số lượng item trong giỏ hàng
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCartCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        Map<String, Object> response = new HashMap<>();
        response.put("count", cartService.getCartItemCount(username));
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách items trong giỏ hàng
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCartItems() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        List<CartItemModel> cartItems = cartService.getCartItems(username);
        BigDecimal total = cartService.getCartTotal(username);

        // Chuyển đổi dữ liệu để trả về client
        List<Map<String, Object>> items = cartItems.stream().map(item -> {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("cartItemId", item.getCartItemId());
            itemData.put("courseId", item.getCourse().getCourseId());
            itemData.put("title", item.getCourse().getTitle());
            itemData.put("thumbnailPath", item.getCourse().getThumbnailPath());
            itemData.put("price", item.getPrice());
            itemData.put("discountPrice", item.getDiscountPrice());
            itemData.put("currentPrice", item.getCurrentPrice());
            itemData.put("quantity", item.getQuantity());
            itemData.put("subtotal", item.getSubtotal());
            return itemData;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("count", items.size());
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    /**
     * Thêm khóa học vào giỏ hàng
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Long courseId = Long.parseLong(request.get("courseId").toString());
            boolean success = cartService.addToCart(courseId, username);

            response.put("success", success);
            if (success) {
                response.put("count", cartService.getCartItemCount(username));
            } else {
                response.put("message", "Không thể thêm khóa học vào giỏ hàng");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật số lượng trong giỏ hàng
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Long courseId = Long.parseLong(request.get("courseId").toString());
            Integer quantity = Integer.parseInt(request.get("quantity").toString());

            boolean success = cartService.updateCartItemQuantity(courseId, quantity, username);
            response.put("success", success);

            if (success) {
                response.put("count", cartService.getCartItemCount(username));
                response.put("total", cartService.getCartTotal(username));
            } else {
                response.put("message", "Không thể cập nhật giỏ hàng");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa khóa học khỏi giỏ hàng
     */
    @DeleteMapping("/remove/{courseId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        Map<String, Object> response = new HashMap<>();

        boolean success = cartService.removeFromCart(courseId, username);
        response.put("success", success);

        if (success) {
            response.put("count", cartService.getCartItemCount(username));
            response.put("total", cartService.getCartTotal(username));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        Map<String, Object> response = new HashMap<>();
        boolean success = cartService.clearCart(username);
        response.put("success", success);

        return ResponseEntity.ok(response);
    }
}