package ar.edu.ubp.das.ristorinoapi.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory liviana para crear y ejecutar {@link SimpleJdbcCall} contra procedimientos almacenados.
 * Centraliza el {@link JdbcTemplate} y estandariza la ejecución y recolección de OUT parameters
 * y result sets devueltos por SQL Server.
 */
@Component
public class SimpleJdbcCallFactory {

    @Autowired
    private JdbcTemplate jdbcTpl;

    /**
     * Ejecuta un procedimiento almacenado devolviendo el mapa de salida tal cual lo entrega SimpleJdbcCall.
     * @param procedureName nombre del procedimiento
     * @param schemaName esquema (por ejemplo, "dbo")
     * @param params parámetros de entrada (puede ser vacío)
     * @return mapa de salida con OUT params y result sets (claves tipo "#result-set-1")
     */
    public Map<String, Object> executeWithOutputs(String procedureName, String schemaName, SqlParameterSource params) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTpl)
                .withProcedureName(procedureName)
                .withSchemaName(schemaName);
        return jdbcCall.execute(params);
    }

    /**
     * Variante sin parámetros de entrada.
     */
    public Map<String, Object> executeWithOutputs(String procedureName, String schemaName) {
        return executeWithOutputs(procedureName, schemaName, new MapSqlParameterSource());
    }

    /**
     * Ejecuta el SP y devuelve todo el mapa OUT, incluyendo juegos de resultados bajo claves tipo "#result-set-1".
     * Útil cuando el SP hace un SELECT final (p.ej. SELECT ... FOR JSON PATH).
     */
    public Map<String, Object> executeReturningEverything(String procedureName, String schemaName, SqlParameterSource params) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTpl)
                .withProcedureName(procedureName)
                .withSchemaName(schemaName);
        return jdbcCall.execute(params);
    }
}
