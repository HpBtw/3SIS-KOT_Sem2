# FIAP To-Do List

## Descrição do projeto e objetivo

O **FIAP To-Do List** é uma aplicação Android desenvolvida em Kotlin para gerenciamento de tarefas.

A aplicação permite cadastrar, visualizar, editar, concluir e excluir tarefas. Cada tarefa pode possuir título, descrição e, opcionalmente, uma data e horário de prazo.

O projeto foi desenvolvido utilizando uma arquitetura baseada na separação de responsabilidades entre interface, gerenciamento de estado e acesso aos dados. A comunicação entre essas camadas é realizada utilizando **ViewModel, Repository, Room, Coroutines e Flow**, enquanto a navegação entre as telas é realizada com **Navigation Compose**.

---

## Tecnologias utilizadas

* **Kotlin** — linguagem utilizada no desenvolvimento da aplicação.
* **Jetpack Compose** — construção da interface gráfica de forma declarativa.
* **Room** — persistência local das tarefas em um banco de dados SQLite.
* **Coroutines** — execução de operações assíncronas, como inserção, atualização e exclusão de tarefas.
* **Flow** — observação reativa das alterações realizadas nos dados.
* **ViewModel** — gerenciamento do estado da aplicação e comunicação entre a interface e o Repository.
* **Navigation Compose** — gerenciamento da navegação entre as telas da aplicação.

---

## Arquitetura da aplicação

A aplicação utiliza uma arquitetura baseada na separação entre interface, estado e dados:

```text
┌─────────────────────────────┐
│       MainActivity          │
│  Cria a ViewModel e inicia  │
│       a navegação           │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       AppNavigation         │
│      Navigation Compose     │
└──────────────┬──────────────┘
               │
       ┌───────┴────────┐
       ▼                ▼
┌──────────────┐  ┌───────────────────┐
│ ListaTarefas │  │ FormularioTarefa │
│    Screen    │  │      Screen       │
└──────┬───────┘  └─────────┬─────────┘
       │                    │
       └─────────┬──────────┘
                 ▼
        ┌──────────────────┐
        │  TarefaViewModel │
        └────────┬─────────┘
                 ▼
        ┌──────────────────┐
        │ TarefaRepository │
        └────────┬─────────┘
                 ▼
        ┌──────────────────┐
        │   Room / DAO     │
        └────────┬─────────┘
                 ▼
        ┌──────────────────┐
        │ Banco de dados   │
        └──────────────────┘
```

Cada camada possui uma responsabilidade específica, evitando que a interface tenha acesso direto ao banco de dados.

---

## TarefaRepository

O `TarefaRepository` é responsável por intermediar o acesso aos dados das tarefas.

Ele recebe o `TarefaDao` e disponibiliza para o restante da aplicação operações relacionadas às tarefas, como:

* consultar as tarefas;
* inserir uma tarefa;
* atualizar uma tarefa;
* excluir uma tarefa.

Dessa forma, o `ViewModel` não precisa acessar diretamente o DAO ou o banco de dados.

O fluxo de acesso aos dados é:

```text
TarefaViewModel
       ↓
TarefaRepository
       ↓
TarefaDao
       ↓
Room
       ↓
SQLite
```

O Repository também permite manter a camada de apresentação independente dos detalhes de implementação da persistência.

---

## TarefaViewModel

O `TarefaViewModel` é responsável por manter e disponibilizar o estado das tarefas para a interface.

Ele recebe o `TarefaRepository` e expõe as tarefas como um estado observável.

O `Flow` proveniente da camada de dados é convertido em um `StateFlow`, permitindo que as telas acompanhem as alterações realizadas no banco.

As operações realizadas pelo ViewModel são executadas utilizando `viewModelScope` e Coroutines.

Entre as operações disponibilizadas estão:

```kotlin
inserir(tarefa)
atualizar(tarefa)
deletar(tarefa)
```

O fluxo é:

```text
Interface
    ↓
TarefaViewModel
    ↓
TarefaRepository
    ↓
TarefaDao
    ↓
Room
```

Assim, a interface não precisa conhecer os detalhes de como os dados são armazenados.

---

## ListaTarefasScreen

A `ListaTarefasScreen` é responsável por apresentar as tarefas cadastradas.

A tela observa o estado disponibilizado pelo `TarefaViewModel` utilizando o estado reativo fornecido pelo Flow/StateFlow.

O fluxo de atualização da interface ocorre da seguinte maneira:

```text
Room
  ↓
TarefaDao
  ↓
TarefaRepository
  ↓
TarefaViewModel
  ↓
StateFlow
  ↓
ListaTarefasScreen
```

Quando os dados são alterados no banco, o estado é atualizado e o Compose recompõe a interface automaticamente.

A tela também dispara ações por meio do `ViewModel`, como:

* criar uma nova tarefa;
* editar uma tarefa;
* alterar o status de conclusão;
* excluir uma tarefa.

Por exemplo, ao marcar uma tarefa como concluída, a tela dispara uma ação que chega ao ViewModel, que solicita ao Repository a atualização da tarefa.

A `ListaTarefasScreen` é, portanto, responsável principalmente pela apresentação dos dados e pelo encaminhamento das ações do usuário.

---

## FormularioTarefaScreen

A `FormularioTarefaScreen` é utilizada tanto para **cadastrar** quanto para **editar** uma tarefa.

A diferenciação é feita através do ID recebido pela tela.

O comportamento é:

```text
tarefaId == 0
     ↓
Cadastro
     ↓
Cria uma nova Tarefa
     ↓
ViewModel.inserir()
```

Enquanto:

```text
tarefaId != 0
     ↓
Edição
     ↓
Localiza a tarefa pelo ID
     ↓
Atualiza seus dados
     ↓
ViewModel.atualizar()
```

No modo de edição, os dados existentes da tarefa são carregados e utilizados como valores iniciais do formulário.

No modo de cadastro, os campos são iniciados vazios.

Dessa forma, a mesma tela pode ser reutilizada para as duas operações sem a necessidade de criar uma tela separada para cadastro e outra para edição.

---

## AppNavigation

A navegação da aplicação é centralizada no `AppNavigation`, utilizando **Navigation Compose**.

São configuradas rotas para as telas principais da aplicação:

```text
Lista de tarefas
       ↓
Formulario de tarefa
```

A rota da lista representa a tela principal:

```text
lista
```

Para criar uma nova tarefa, a navegação utiliza a rota do formulário sem um ID de tarefa:

```text
formulario
```

Para editar uma tarefa existente, o ID é incluído na rota:

```text
formulario/{tarefaId}
```

O fluxo de edição é:

```text
ListaTarefasScreen
       │
       │ usuário seleciona uma tarefa
       │
       ▼
     ID da tarefa
       │
       ▼
AppNavigation
       │
       ▼
FormularioTarefaScreen
       │
       ▼
Recebe tarefaId
```

O ID permite que o `FormularioTarefaScreen` identifique qual tarefa deve ser carregada e alterada.

Assim, a navegação não precisa transportar o objeto inteiro da tarefa entre as telas. Apenas o identificador é passado pela rota, e a tarefa é localizada através do estado disponibilizado pelo ViewModel.

---

## MainActivity

A `MainActivity` é o ponto inicial da aplicação Android.

Sua responsabilidade é configurar o conteúdo da aplicação utilizando Jetpack Compose, criar a instância do `TarefaViewModel` e iniciar o componente responsável pela navegação.

O fluxo inicial da aplicação é:

```text
Android inicia a aplicação
          ↓
      MainActivity
          ↓
   Criação da ViewModel
          ↓
     AppNavigation
          ↓
 ListaTarefasScreen
```

A `MainActivity` não concentra a lógica de negócio nem as operações de banco de dados.

Ela funciona como ponto de entrada da aplicação e conecta a infraestrutura inicial à navegação Compose.

---

## Execução do projeto

### Pré-requisitos

Para executar o projeto é necessário ter instalado:

* Android Studio;
* Android SDK;
* JDK compatível com a configuração do projeto;
* um dispositivo Android físico ou um emulador.

### Executando pelo Android Studio

1. Abra o projeto no **Android Studio**.
2. Aguarde a sincronização do Gradle.
3. Certifique-se de que o módulo `app` está configurado para execução.
4. Inicie um emulador Android ou conecte um dispositivo físico.
5. Selecione o dispositivo no Android Studio.
6. Clique em **Run ▶**.
7. Aguarde a compilação e instalação da aplicação.

A aplicação será iniciada pela `MainActivity` e a navegação será carregada através do `AppNavigation`.

---

<img width="277" height="356" alt="image" src="https://github.com/user-attachments/assets/a4654476-cd7b-442b-b1d2-a394d67f9b9c" />

<img width="285" height="269" alt="image" src="https://github.com/user-attachments/assets/e341130b-6cc4-40ec-abe9-977093698b4a" />

