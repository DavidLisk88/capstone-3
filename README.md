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



## 






