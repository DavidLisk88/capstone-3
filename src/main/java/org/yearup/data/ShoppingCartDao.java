package org.yearup.data;

import org.yearup.models.ShoppingCart;

import java.security.Principal;

public interface ShoppingCartDao
{
    ShoppingCart getCart(int userId, Principal principal);
    ShoppingCart getByUserId(int userId);
    void addItem (int userId, int productId);
    void updateQuantity(int userId, int productId, int quantity);
    void clearCart(int userId);
    // add additional method signatures here




}
