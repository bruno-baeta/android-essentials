# Android Essentials  

Android Essentials is a library that provides architectural patterns to streamline Android app development. It includes state management, mappers, validators and filters patterns to ensure code modularity and reusability.

## Index

1. [Installation](#installation)
2. [State Management](#state-management)
    - [Defining State](#defining-state)
    - [Defining Actions](#defining-actions)
    - [Using EssentialsViewModel](#using-essentialsviewmodel)
    - [Observing actions and state](#observing-actions-and-state)
3. [Validators](#validators)
    - [Defining Validators](#defining-validators)
    - [Using Validators](#using-validators)
4. [CompositeValidator](#compositevalidator)
    - [Using CompositeValidator](#using-compositvalidator)
5. [Mapper](#mappers)
    - [Defining a Mapper](#defining-a-mapper)
    - [Using a Mapper](#using-a-mapper)
6. [MapperException](#mapperexception)
    - [Defining a MapperException](#defining-a-mapperexception)
    - [Using a MapperException](#using-a-mapperexception)
7. [Filters](#filters)
    - [Defining Filters](#defining-filters)
    - [Using Filter](#using-filter)
9. [Contributing](#contributing)
11. [Contact and Support](#contact-and-support)

## Installation

To include Android Essentials in your project, add the following to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.barbosa.essentials:android-essentials:1.0'
}
```

## State Management

Android Essentials uses reactive programming principles and Kotlin coroutines to handle state management. The main components are:

**StateFlow**: Used to hold and observe the state. It retains the last emitted value, making it ideal for UI state management.

**SharedFlow**: Used to emit one-time events, like navigation actions or error messages. It does not retain the last emitted value, ensuring events are handled correctly.

**Coroutines**: Utilized for managing asynchronous operations efficiently. viewModelScope ensures that these operations are tied to the ViewModel's lifecycle, preventing memory leaks.

### Defining State

State represents the UI's data at any given point in time. It should encapsulate all the information needed to render the UI correctly.

```kotlin
data class UserState(
    val isLoading: Boolean = false,
    val users: List<User> = listOf()
)
```
### Defining Actions

Actions represent one-time events or commands sent to the UI to trigger simple operations that are executed only once. They are encapsulated in a sealed class to ensure type safety and exhaustive checking.

```kotlin
sealed class UserAction {
    data object NavigateToUserDetailing() : UserAction()
    data object ShowErrorMessage() : UserAction()
}
```

### Using EssentialsViewModel

The ViewModel is responsible for managing the state and handling actions. It extends `EssentialsViewModel`, which provides the base implementation for state and action management using StateFlow and SharedFlow.

```kotlin
import com.barbosa.essentials.core.EssentialsViewModel

class UserViewModel(
    private val getUsersUseCase: GetUsersUseCase
) : EssentialsViewModel<UserState, UserAction>(UserState()) {
    
    init {
        getUsers()
    }

    private fun getUsers() {
        viewModelScope.launch {
            getUsersUseCase()
                .flowOn(Dispatchers.IO)
                .catch {
                    sendAction(UserAction.ShowErrorMessage)
                }.collect { users ->
                   updateState { state ->
                        state.copy(
                            users = users
                        )
                   }
                }
        }
    }
}
```

### Observing actions and state

To update the UI based on state changes and handle one-time actions, observe the StateFlow and SharedFlow from the ViewModel.

Use provided extensions to observe actions and state in a fragment or activity and update the UI accordingly.

```kotlin
import com.example.essentials.extensions.observeActions
import com.example.essentials.extensions.observeState

observeActions(viewModel.action) { action ->
    when (action) {
        is UserAction.ShowMessage -> {
            // show error message
        }
        is UserAction.NavigateToUserDetailing -> {
            // navigate to detailing
        }
    }
}

observeState(viewModel.state) { state ->
    // Update your UI accordingly to the state
}
```

## Validators

The Validator interface allows you to implement custom validation logic for objects of type T. It includes methods for validating single objects (validate) and lists of objects (validateList). This ensures consistent, reusable validation logic across your application.

### Defining Validators
Here’s how to implement the Validator interface for a specific object type:

```kotlin
import com.barbosa.essentials.core.Validator

class UserGmailValidator: Validator<User> {
    override fun validate(value: User): Boolean = with(value) {
        return email.contains("@gmail.com")
    }
}

class UserMajorityAgeValidator: Validator<User> {
    override fun validate(value: User): Boolean = with(value) {
        return age in 0..18
    }
}
```

### Using Validators

```kotlin
import com.barbosa.essentials.core.Validator

class AuthenticateUserUseCaseImpl(
    private val userIdValidator: Validator<User>,
    private val repository: UserRepository
): AuthenticateUserUseCase {

   operator fun invoke(user: User) {
        if (userAgeValidator.validate(user)) {
            repository.authenticateUser(user)
        } else {
            throw InvalidUserException()
        }
    }
}
```

#### Methods
-   **validate(value: T): Boolean** - Validates a single object.
-   **validateList(values: List\<T>): Boolean** - Validates a list of objects.

## CompositeValidator

Designed to simplify the process of validating complex objects. It allows you to combine multiple validators into a single, cohesive validation mechanism

```kotlin
import com.barbosa.essentials.core.Validator
import com.barbosa.essentials.core.CompositeValidator

class UserValidator(
    private val emailValidator: Validator<User>,
    private val ageValidator: Validator<User>
): Validator<User> {

    private val composite = CompositeValidator(
        listOf(emailValidator, ageValidator)
    )

    override fun validate(value: User): Boolean = with(value) {
        return composite.validate()
    }
}
```

### Using CompositValidator

```kotlin
import com.example.essentials.core.Validator

class AuthenticateUserUseCaseImpl(
    private val userValidator: Validator<User>,
    private val repository: UserRepository
): AuthenticateUserUseCase {

   operator fun invoke(user: User) {
        if (userValidator.validate(user)) {
            repository.authenticateUser(user)
        } else {
            throw InvalidUserException()
        }
    }
}
```
This approach ensures that your validation logic is not only consistent and reusable but also neatly separated into distinct, manageable units, enhancing the overall readability and maintainability of your code.

## Mappers

The Mapper interface is designed to convert objects from one type (I) to another type (O). This is particularly useful in applications that require data transformations between different layers, such as converting database entities or response objects to domain.

### Defining a Mapper

To define a mapper, implement the `Mapper` interface:

```kotlin
import com.example.essentials.core.ValidatorMapper

class UserMapper : Mapper<UserResponse, User> {
    override fun map(input: UserResponse): User = with(input) {
        return User(
            id = id, 
            name = name,
            age = age
        )
    }
}
```

### Using a Mapper

To use the mapper, create an instance and call the `map` method:

```kotlin
import com.barbosa.essentials.core.Mapper

class UserRepositoryImpl(
    private val userService: UserService,
    private val userMapper: Mapper<UserResponse, User>
) : UserRepository {

    override fun getUser(id: Int): Flow<User> {
        val userResponse = userService.getUser(id) 
        return userMapper.map(userResponse)
    }

    override fun getUsers(): Flow<List<User>> {
        val usersResponse = userService.getUsers() 
        return userResponseToUserMapper.mapList(usersResponse)
    }
}
```

#### Methods
-   **map(value: I): O** - map a single object.
-   **mapList(values: List\<I>): O** - map a list of objects.


## MapperException

The MapperExceptions interface is designed to provide a standardized way to map various types of errors to user-defined, domain-specific exceptions. By implementing this interface, you can create custom error handling logic tailored to the needs of your application.

### Defining a MapperException

Implement the MapperExceptions interface to define how different types of Throwable instances are mapped to specific exceptions in your domain. This approach allows you to handle errors in a consistent and meaningful way throughout your application.

```kotlin
import com.barbosa.essentials.core.MapperExceptions

class UserRemoteMapperExceptions : MapperExceptions {
    
    override fun map(throwable: Throwable): Throwable = when (throwable) {
        is HttpException -> parseHttpException(throwable)
        is IOException -> parseIOException(throwable)
        is IllegalArgumentException -> parseIllegalArgumentException(throwable)
        else -> throwable
    }

    private fun parseHttpException(exception: Throwable): Throwable {
        return when (exception.code()) {
            404 -> UserNotFoundException()
            else -> exception
        }
    }

    private fun parseIOException(exception: Throwable): Throwable {
        return MyNetworkException()
    }

    private fun parseIllegalArgumentException(exception: Throwable): Throwable {
        return MyInvalidInputException()
    }
}
```

### Using a MapperException

To handle exceptions in a Kotlin Flow using the MapperExceptions interface, you can use the extension parseDataExceptions. This function uses the catch operator to intercept exceptions and map them using a provided MapperExceptions implementation.

```kotlin
import com.barbosa.essentials.core.MapperExceptions
import com.barbosa.essentials.extensions.parseDataExceptions

class UserRepository(
    private val userService: UserService,
    private val dataExceptionMapper: MapperExceptions
) {

    fun fetchUserData(): Flow<String> {
        return userService.getUserData().parseDataExceptions(dataExceptionMapper)
    }
}
```

## Filters

The Filter interface is designed to provide a standardized way to implement custom filtering logic for collections of objects of type T. By implementing this interface, you can encapsulate the filtering logic and apply it consistently across your application.

### Defining Filters

Implement the Filter interface to define specific filtering criteria for your objects. This separation of concerns ensures that each class focuses on a single responsibility, enhancing the maintainability and readability of your code.

```kotlin
import com.barbosa.essentials.core.Filter

class FilterUsersUnderAge() : Filter<User> {
    
    override fun filter(items: List<User>): List<User> {
        return items.filter { it.age < 18 }
    }
}

class FilterUsersByEmail : Filter<User> {
    
    override fun filter(items: List<User>): List<User> {
        return items.filter { it.email.contains("@gmail.com") }
    }
}
```

### Using filter
This design promotes flexibility and reusability. You can easily swap different filters without changing the core logic, adapting to new requirements seamlessly.
Encapsulating filtering logic within the use case also simplifies testing. Mock implementations of the repository and filter can be used to test the filtering logic independently, ensuring more reliable and maintainable tests.

```kotlin
class GetUnderAgeUsersUseCaseImpl(
    private val userRepository: UserRepository,
    private val underAgeFilter: Filter<User>
): GetUnderAgeUsersUseCase {

    operator fun invoke(): Flow<List<User>> {
        return underAgeFilter.filter(userRepository.getUsers())
    }
}
```
## Contributing

Contributions are welcome! If you would like to contribute to Android Essentials, please follow these steps:

1. Fork the repository.
2. Create a branch for your feature (`git checkout -b feature/new-feature`).
3. Commit your changes (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature/new-feature`).
5. Open a Pull Request.

## Contact and Support

If you have any questions, issues, or suggestions, feel free to open an issue on GitHub or contact us via email at jbruno356@gmail.com.