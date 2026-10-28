package com.example.cupbank.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cupbank.Movimento
import com.example.cupbank.TransacaoCartao
import com.example.cupbank.investimentos.Ativo
import com.example.cupbank.investimentos.Ordem

@Database(
    entities = [Ativo::class, Ordem::class, TransacaoCartao::class, Movimento::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ativoDao(): AtivoDao
    abstract fun ordemDao(): OrdemDao
    abstract fun transacaoCartaoDao(): TransacaoCartaoDao
    abstract fun movimentoDao(): MovimentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cupbank_database"
                )
                    .addMigrations(MIGRACAO_1_2)
                    .addCallback(DadosIniciais)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

private const val SQL_MOVIMENTOS_INICIAIS = """INSERT INTO movimentos (titulo, detalhe, valor, data) VALUES
               ('Salário', 'Depósito', 2800.00, '05/10'),
               ('Conta de luz', 'Pagamento', -142.30, '06/10'),
               ('PIX recebido', 'De: Maria Souza', 250.00, '07/10')"""

private val MIGRACAO_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `movimentos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`titulo` TEXT NOT NULL, `detalhe` TEXT NOT NULL, `valor` REAL NOT NULL, `data` TEXT NOT NULL)"
        )
        db.execSQL(SQL_MOVIMENTOS_INICIAIS)
    }
}

private object DadosIniciais : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL(
            """INSERT INTO ativos (ticker, nome, preco, variacao, tipo) VALUES
       ('PETR4', 'Petrobras PN', 38.42, 1.84, 'ACAO')"""
        )
        db.execSQL(
            """INSERT INTO transacoes_cartao (descricao, valor, data, categoria, pago) VALUES
               ('Supermercado Extra', 87.50, '05 de out.', 'Alimentação', 0),
               ('Uber', 25.00, '06 de out.', 'Transporte', 0),
               ('Netflix', 45.90, '07 de out.', 'Entretenimento', 0),
               ('Restaurante Bom Sabor', 120.00, '07 de out.', 'Alimentação', 0),
               ('Posto Shell', 200.00, '08 de out.', 'Combustível', 0)"""
        )
        db.execSQL(SQL_MOVIMENTOS_INICIAIS)
    }
}
