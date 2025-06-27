# EASY SHOP
_______________________________

## Register 
"username": "david",
"password": "password",
"confirmPassword": "password",
"role": "ROLE_USER"


## Logging in 
"username": "david",
"password": "password"

_______________________________


## Categories Controller

### Annotations


```java
// add the annotations to make this a REST controller
// add the annotation to make this controller the endpoint for the following url
    // http://localhost:8080/categories
// add annotation to allow cross site origin requests
@RestController
@RequestMapping("/categories")
public class CategoriesController

```


### Autowire and Inject

```java
    private CategoryDao categoryDao;
    private ProductDao productDao;



    // create an Autowired controller to inject the categoryDao and ProductDao
    @Autowired
    public CategoriesController(DatabaseConfig config) throws SQLException{
        this.categoryDao = new MySqlCategoryDao(config.dataSource());
        this.productDao = new MySqlProductDao(config.dataSource());
    }
```


### Get all categories 

```java
    // add the appropriate annotation for a get action
    @GetMapping
    // Change "int" to "Integer". int cannot be null. Integer is an object that works like a box and can be null.
    @PreAuthorize("permitAll()")
    public List<Category> getAllCategories(@RequestParam(required = false) Integer categoryId, @RequestParam(required = false) String name)
    {
        // find and return all categories

        List<Category> allCategories = categoryDao.getAllCategories(categoryId, name);
        List<Category> categoryResults = new ArrayList<>();

        // for every category in the array list,
        // if the categoryId in the parameter(url) is not specified,
        // if specified parameters are not found, then no categories are found
        // otherwise, add all found categories to the categoryResults array
        for (Category categoryList : allCategories){
          boolean foundCategories = true;

          if (categoryId != null && categoryList.getCategoryId() != categoryId){
              foundCategories = false;
            }
          if (name != null && !categoryList.getName().equalsIgnoreCase(name)){
              foundCategories = false;
          }
          if (foundCategories){
              categoryResults.add(categoryList);
          }
        }
        // now we return the category results.
        return categoryResults;
    }
```

Able to use " http://localhost:8080/categories?categoryId=1 " OR " http://localhost:8080/categories/1 ". 

```java
    // add the appropriate annotation for a get action
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Category getById(@PathVariable int id)
    {
        // get the category by id
        Category foundCategory = categoryDao.getById(id);
        System.out.println("Found: " + foundCategory);

        return foundCategory;
    }
```


### Get products by category ID

```java
    // the url to return all products in category 1 would look like this
    // https://localhost:8080/categories/1/products
    @GetMapping("/{categoryId}/products")
    @PreAuthorize("permitAll()")
    public List<Product> getProductsById(@PathVariable Integer categoryId)
    {
        // get a list of product by categoryId
        List<Product> products = productDao.listByCategoryId(categoryId);
        List<Product> productResults = new ArrayList<>();

        for (Product product : products){
            boolean found = true;

            if (product.getCategoryId() != categoryId){
                found = false;
            }
            if (found){
                productResults.add(product);
            }
        }
        return productResults;
    }
```



### Add, update & delete

```java
    // add annotation to call this method for a POST action
    // add annotation to ensure that only an ADMIN can call this function
    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Category create (@RequestBody Category category)
    {
        // insert the category
        Category newCategory = categoryDao.create(category);
        System.out.println("Added new category: " + newCategory);
        return newCategory;

    }

    // add annotation to call this method for a PUT (update) action - the url path must include the categoryId
    // add annotation to ensure that only an ADMIN can call this function
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        // update the category by id
        categoryDao.updateCategory(id, category);
        if (category == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }


    // add annotation to call this method for a DELETE action - the url path must include the categoryId
    // add annotation to ensure that only an ADMIN can call this function
    @DeleteMapping("/delete/{categoryId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCategory(@PathVariable int categoryId)
    {
        // delete the category by id
        System.out.println("Deleted: " + getById(categoryId));
        categoryDao.deleteCategory(categoryId);
    }
}
```


Show new and updated category in postman. 



## Product Bugs


### Fix output for ListByCategoryId

```java
    @GetMapping("/cat/{categoryId}")
    @PreAuthorize("permitAll()")
    public List<Product> listByCategoryId(@PathVariable Integer categoryId )
    {

        try
        {
            List<Product> products = productDao.listByCategoryId(categoryId);

            if(products == null || products.isEmpty())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            return products;
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
```



### Fix product search functionality 

```java
    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color)
    {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products " +
                "WHERE (category_id = ? OR ? = -1) " +
                "AND (price >= ? OR ? = -1)" +
                "   AND (price <= ? OR ? = -1) " +
                "   AND (color = ? OR ? = '') ";

        categoryId = categoryId == null ? -1 : categoryId;
        minPrice = minPrice == null ? new BigDecimal("-1") : minPrice;
        maxPrice = maxPrice == null ? new BigDecimal("-1") : maxPrice;
        color = color == null ? "" : color;

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);
            statement.setInt(2, categoryId);
            statement.setBigDecimal(3, minPrice);
            statement.setBigDecimal(4, minPrice);
            statement.setBigDecimal(5, maxPrice);
            statement.setBigDecimal(6, maxPrice);
            statement.setString(7, color);
            statement.setString(8, color);

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }
```




### Make sure update product does not add a new product 

```java
/// CONTROLLER

    @PutMapping("/update/{productId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateProduct(@PathVariable int productId, @RequestBody Product product)
    {
        try
        {
            productDao.updateProduct(productId, product);
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


/// ______________________
/// DAO 

   @Override
    public void updateProduct(int productId, Product product)
    {
        String sql = "UPDATE products" +
                " SET name = ? " +
                "   , price = ? " +
                "   , category_id = ? " +
                "   , description = ? " +
                "   , color = ? " +
                "   , image_url = ? " +
                "   , stock = ? " +
                "   , featured = ? " +
                " WHERE product_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
```



## Shopping Cart 


### Create REST methods for shopping cart

```java
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
```



### Use Post to add a new product 
http://localhost:8080/cart/products/16

### Use PUT to update quantity 
"quantity": "2" in JSON file



## User Profile

### Implement a getByUserId method and Update method

```java
@Override
    public void updateProfile (Profile profile) throws SQLException {
        String sql = "UPDATE profiles SET first_name = ?, last_name = ?, " +
                "phone = ?, email = ?, address = ?, city = ?, state = ?, zip = ? " +
                "WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, profile.getFirstName());
            statement.setString(2, profile.getLastName());
            statement.setString(3, profile.getPhone());
            statement.setString(4, profile.getEmail());
            statement.setString(5, profile.getAddress());
            statement.setString(6, profile.getCity());
            statement.setString(7, profile.getState());
            statement.setString(8, profile.getZip());
            statement.setInt(9, profile.getUserId());
            statement.executeUpdate();
        }



}

@Override
public Profile getByUserId (int userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, userId);
            ResultSet results = statement.executeQuery();
            if (results.next()){
                Profile profile = new Profile();
                profile.setUserId(results.getInt("user_id"));
                profile.setFirstName(results.getString("first_name"));
                profile.setLastName(results.getString("last_name"));
                profile.setPhone(results.getString("phone"));
                profile.setEmail(results.getString("email"));
                profile.setAddress(results.getString("address"));
                profile.setCity(results.getString("city"));
                profile.setState(results.getString("state"));
                profile.setZip(results.getString("zip"));
                return profile;

            } else {
                return null ;
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
      return null;
}
```



### Implement ProfileController

```java
    @GetMapping
    public Profile getByUserId(){
try {
    User user = getCurrentUser();

    Profile profile = profileDao.getByUserId(user.getId());

    if (profile == null){
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
    }
    return profileDao.getByUserId(profile.getUserId());

} catch (Exception e){
    throw new ResponseStatusException( HttpStatus.INTERNAL_SERVER_ERROR, "Error");
}
    }




    @PostMapping
    public Profile create(@RequestBody Profile profile) {
        try {
            User user = getCurrentUser();
            profile.setUserId(user.getId());

            Profile existingProfile = profileDao.getByUserId(user.getId());
            if (existingProfile != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists");
            }

            return profileDao.create(profile);

        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating profile");
        }
    }

    @PutMapping
    public void update (@RequestBody Profile profile){
        try{
            User user = getCurrentUser();
            profile.setUserId(user.getId());

            profileDao.updateProfile(profile);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error");
        }
    }


}
```

____________________________________________________________________________________________________________

## BONUS CODE

#### Added a getCurrentUser method to authenticate the current logged in user 

```java
    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userDao.getByUserName(username);
    }
```



