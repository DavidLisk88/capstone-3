package org.yearup.data;

import org.yearup.models.ShoppingCart;

import java.security.Principal;

public interface ShoppingCartDao
{
    ShoppingCart getCart(int userId, Principal principal);
    // add additional method signatures here




}
