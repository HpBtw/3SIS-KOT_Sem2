package hpbtw.com.github.todolist.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room3.Entity

@Entity(tableName = "tarefas")
data class Tarefa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descricao: String,
    val concluida: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis()
)