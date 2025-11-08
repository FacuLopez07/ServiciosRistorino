package ar.edu.ubp.das.ristorinoapi.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SimpleJdbcCallFactory {

    @Autowired
    private JdbcTemplate jdbcTpl;

    public Map<String, Object> executeWithOutputs(String procedureName, String schemaName, SqlParameterSource params) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTpl)
                .withProcedureName(procedureName)
                .withSchemaName(schemaName);
        return jdbcCall.execute(params);
    }

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
