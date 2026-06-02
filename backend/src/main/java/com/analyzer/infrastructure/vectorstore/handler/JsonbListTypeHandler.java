package com.analyzer.infrastructure.vectorstore.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
@MappedTypes(List.class)
public class JsonbListTypeHandler extends BaseTypeHandler<List<String>> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        try {
            pgObject.setValue(MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Failed to serialize keywords", e);
        }
        ps.setObject(i, pgObject);
    }
    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }
    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }
    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }
    private List<String> parse(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
