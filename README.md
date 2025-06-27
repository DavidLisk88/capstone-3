# EASY SHOP
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


### 






