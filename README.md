# To Do List Backend
## Getting Started
### This application contains the backend for a To Do Application. It is made using SpringBoot. The app handles the following operations:
- #### Get all ToDos in the Database
- #### Get ToDos based on filters, such as # of pages, name, priority, status, the sorting direction and what to sort by.
- #### Create new ToDos
- #### Update the current ToDos
- #### Update the status of the ToDos
- #### Delete ToDos
- #### Retrieve metrics

--- 

## Architecture

---

### Controller
#### This app works by first defining a controller using SpringBoot.
#### This controller defines the different api routes using the tags @PutMapping, @GetMapping, @DeleteMapping, and @PostMapping
#### The tag @CrossOrigin is placed in front of all routes in order to allow connections from the frontend development port 8080

---

### Service Layer
#### The service layer defines the methods and types to be used for the api routes.

---

### Implementation Layer
#### The implementation layer does the necessary operations to fulfill the api routes requirements.
#### A dynamic list with mock To Do objects is defined in the implementation layer, this data is then used for the api routes.

---

### Entities
#### To Do Entity:
- ##### The To Do entity is a blueprint for the ToDo object, and is defined as follows: long id, String name, String description, String priority, Boolean status, LocalDate dueDate, LocalDate doneDate, LocalDate creationDate
#### Metrics Entity:
- ##### The Metrics entity is used for the return object Metrics. It is defined as: long totalAverage, long highAverage, long mediumAverage, long lowAverage

---
## Tests
### Testing is done using the Test Rest Template which simulates use from the web.
#### Tests are done by calling the api as expected and comparing the response with the expected response. This are found inside HttpRequestTest.
### Two other tests are run, a Smoke Test, and a test that validates the application is not null.

