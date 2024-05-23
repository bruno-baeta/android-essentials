# Android Essentials  ([en-us](README-en.md))

Android Essentials é uma biblioteca que fornece padrões arquiteturais para simplificar o desenvolvimento de aplicativos Android. Inclui gerenciamento de estado, mapeadores, validadores e filtros para garantir modularidade e reutilização do código.

## Índice

1. [Instalação](#instalação)
2. [Gerenciamento de Estado](#gerenciamento-de-estado)
    - [Definindo Estado](#definindo-estado)
    - [Definindo Ações](#definindo-ações)
    - [Usando EssentialsViewModel](#usando-essentialsviewmodel)
    - [Observando ações e estado](#observando-ações-e-estado)
3. [Validadores](#validadores)
    - [Definindo Validadores](#definindo-validadores)
    - [Usando Validadores](#usando-validadores)
4. [CompositeValidator](#compositevalidator)
    - [Usando CompositeValidator](#usando-compositevalidator)
5. [Mapeadores](#mapeadores)
    - [Definindo um Mapeador](#definindo-um-mapeador)
    - [Usando um Mapeador](#usando-um-mapeador)
6. [MapperException](#mapperexception)
    - [Definindo um MapperException](#definindo-um-mapperexception)
    - [Usando um MapperException](#usando-um-mapperexception)
7. [Filtros](#filtros)
    - [Definindo Filtros](#definindo-filtros)
    - [Usando Filtros](#usando-filtros)
9. [Contribuição](#contribuição)
10. [Contato e Suporte](#contato-e-suporte)

## Instalação

Para incluir Android Essentials em seu projeto, adicione o seguinte ao seu `build.gradle`:

```gradle
dependencies {
    implementation 'com.barbosa.essentials:android-essentials:1.0'
}
```

## Gerenciamento de Estado

Android Essentials usa princípios de programação reativa e corrotinas do Kotlin para lidar com o gerenciamento de estado. Os principais componentes são:

**StateFlow**: Usado para manter e observar o estado. Ele retém o último valor emitido, tornando-o ideal para gerenciamento de estado de UI.

**SharedFlow**: Usado para emitir eventos únicos, como ações de navegação ou mensagens de erro. Não retém o último valor emitido, garantindo que os eventos sejam tratados corretamente.

**Coroutines**: Utilizado para gerenciar operações assíncronas de forma eficiente. viewModelScope garante que essas operações estejam vinculadas ao ciclo de vida do ViewModel, evitando vazamentos de memória.

### Definindo Estado

State representa os dados da UI em qualquer momento. Deve encapsular todas as informações necessárias para renderizar a UI corretamente.

```kotlin
data class UserState(
    val isLoading: Boolean = false,
    val users: List<User> = listOf()
)
```

### Definindo Ações

Ações representam eventos únicos ou comandos enviados à UI para acionar operações simples que são executadas apenas uma vez. Elas são encapsuladas em uma classe selada para garantir a segurança de tipo e verificação exaustiva.

```kotlin
sealed class UserAction {
    data object NavigateToUserDetailing() : UserAction()
    data object ShowErrorMessage() : UserAction()
}
```

### Usando EssentialsViewModel

O ViewModel é responsável por gerenciar o estado e lidar com as ações. Ele estende `EssentialsViewModel`, que fornece a implementação base para gerenciamento de estado e ações usando StateFlow e SharedFlow.

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

### Observando ações e estado

O exemplo mostra como podemos observar as actions e o state com as extensions `observeActions` e `observeState`:

```kotlin
import com.barbosa.essentials.extensions.observeActions
import com.barbosa.essentials.extensions.observeState

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

## Validadores

A interface Validator permite que você implemente lógica de validação personalizada para qualquer objeto. Inclui métodos para validar objetos únicos (validate) e listas de objetos (validateList). Isso garante uma lógica de validação consistente e reutilizável em toda a sua aplicação.

### Definindo Validadores

Veja como implementar a interface Validator para um tipo de objeto específico:

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

### Usando Validadores

```kotlin
import com.barbosa.essentials.core.Validator

class AuthenticateUserUseCaseImpl(
    private val validateUserMajority: Validator<User>,
    private val repository: UserRepository
): AuthenticateUserUseCase {

   operator fun invoke(user: User) {
        if (validateUserMajority.validate(user)) {
            repository.authenticateUser(user)
        } else {
            throw InvalidUnderAgeException()
        }
    }
}
```

#### Métodos
-   **validate(value: T): Boolean** - Valida um único objeto.
-   **validateList(values: List\<T>): Boolean** - Valida uma lista de objetos.

## CompositeValidator

Projetado para simplificar o processo de validação de objetos complexos. Permite combinar vários validadores em um único mecanismo de validação coeso.

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

### Usando CompositeValidator

```kotlin
import com.barbosa.essentials.core.Validator

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

Essa abordagem garante que sua lógica de validação não seja apenas consistente e reutilizável, mas também ordenadamente separada em unidades distintas e gerenciáveis, melhorando a legibilidade e a manutenibilidade do seu código.

## Mapeadores

A interface Mapper é projetada para converter objetos de um tipo (I) para outro tipo (O). Isso é particularmente útil em aplicativos que exigem transformações de dados entre diferentes camadas, como converter entidades de banco de dados ou objetos de resposta para domínio.

### Definindo um Mapeador

Para definir um mapeador, implemente a interface `Mapper`:

```kotlin
import com.barbosa.essentials.core.ValidatorMapper

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

### Usando um Mapeador

Para usar o mapeador, crie uma instância e chame o método `map`:

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

#### Métodos
-   **map(value: I): O** - Mapeia um único objeto.
-   **mapList(values: List\<I>): O** - Mapeia uma lista de objetos.

## MapperException

A interface MapperExceptions é projetada para fornecer uma maneira padronizada de mapear vários tipos de erros para exceções específicas do domínio do usuário. Implementando essa interface, você pode criar lógica de tratamento de erros personalizada adaptada às necessidades do seu aplicativo.

### Definindo um MapperException

Implemente a interface MapperExceptions para definir como diferentes tipos de instâncias Throwable são mapeados para exceções específicas no seu domínio. Essa abordagem permite que você lide com erros de maneira consistente e significativa em todo o seu aplicativo.

```kotlin
import com.barbosa.essentials.core.MapperExceptions

class UserRemoteMapperExceptions : MapperExceptions {
    
    override fun map(throwable: Throwable): Throwable = when (throwable) {
        is HttpException -> parseHttpException(throwable)
        is IOException -> parseIOException(throwable)
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
}
```

### Usando um MapperException

Para lidar com exceções em um Kotlin Flow usando a interface MapperExceptions, você pode usar a extensão parseDataExceptions. Esta função utiliza o operador catch para interceptar exceções e mapeá-las usando uma implementação fornecida de MapperExceptions.

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

## Filtros

A interface Filter é projetada para fornecer uma maneira padronizada de implementar lógica de filtragem personalizada para coleções de objetos. Implementando esta interface, você pode encapsular a lógica de filtragem e aplicá-la de forma consistente em toda a sua aplicação.

### Definindo Filtros

Essa separação de preocupações garante que cada classe se concentre em uma única responsabilidade, melhorando a manutenibilidade e a legibilidade do código.

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

### Usando Filtros

Este design promove flexibilidade e reutilização. Você pode facilmente trocar diferentes filtros sem alterar a lógica principal, adaptando-se a novos requisitos sem problemas. Encapsular a lógica de filtragem dentro do caso de uso também simplifica os testes. Implementações simuladas do repositório e filtro podem ser usadas para testar a lógica de filtragem de forma independente, garantindo testes mais confiáveis e manuteníveis.

```kotlin
import com.barbosa.essentials.core.Filter

class GetUnderAgeUsersUseCaseImpl(
    private val userRepository: UserRepository,
    private val underAgeFilter: Filter<User>
): GetUnderAgeUsersUseCase {

    operator fun invoke(): Flow<List<User>> {
        return underAgeFilter.filter(userRepository.getUsers())
    }
}
```
## Contribuição

Contribuições são bem-vindas! Se você quiser contribuir com Android Essentials, siga estas etapas:

1. Fork o repositório.
2. Crie um branch para sua feature (`git checkout -b feature/nova-feature`).
3. Commit suas alterações (`git commit -m 'Adiciona nova feature'`).
4. Faça o push para o branch (`git push origin feature/nova-feature`).
5. Abra um Pull Request.


## Contato e Suporte

Se você tiver alguma dúvida, problema ou sugestão, sinta-se à vontade para abrir uma issue no GitHub ou entrar em contato através do email jbruno356@gmail.com.