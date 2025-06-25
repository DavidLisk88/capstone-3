package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;
import java.util.Map;

// convert this class to a REST controller
// only logged in users should have access to these actions
@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController
{
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    @Autowired
    public ShoppingCartController(UserDao userDao, ProductDao productDao, ShoppingCartDao shoppingCartDao){
        this.userDao = userDao;
        this.productDao = productDao;
        this.shoppingCartDao = shoppingCartDao;
    }

    // Helper method to authenticate the user that is logged in
    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userDao.getByUserName(username);
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // get the currently logged in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();


            // use the shoppingcartDao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        { e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{productId}")
    public ShoppingCart addItem(@PathVariable int productId)
    {
        try
        {

            User user = getCurrentUser();
            int userId = user.getId();


            shoppingCartDao.addItem(userId, productId);
            return shoppingCartDao.getByUserId(user.getId());
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add item to cart.");
        }
    }



    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping("/products/{productId}")
    public ShoppingCart updateQuantity(@PathVariable int productId, @RequestBody Map<String, Integer> body){

        // Error handling, if there is no quantity found to update
        if(!body.containsKey("quantity")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No quantity found");
        }
        // Grab the quantity from the body map, find the user, and then update the information of the product from that user.
        int quantity = body.get("quantity");
        User user = getCurrentUser();
        shoppingCartDao.updateQuantity(user.getId(), productId, quantity);
        return shoppingCartDao.getByUserId(user.getId());
    }


    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart

    @DeleteMapping
    public ShoppingCart clearCart(){
        User user = getCurrentUser();
        shoppingCartDao.clearCart(user.getId());
        return shoppingCartDao.getByUserId(user.getId());
    }

}
