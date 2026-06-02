package com.analyzer.infrastructure.vectorstore.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.postgresql.util.PGobject;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
@MappedJdbcTypes(JdbcType.OTHER)
public class VectorTypeHandler extends BaseTypeHandler<float[]> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(toVectorString(parameter));
        ps.setObject(i, pgObject);
    }
    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseVector(rs.getString(columnName));
    }
    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseVector(rs.getString(columnIndex));
    }
    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseVector(cs.getString(columnIndex));
    }
    private String toVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
    private float[] parseVector(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String stripped = value.substring(1, value.length() - 1); // 去掉 [ ]
        String[] parts = stripped.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}
