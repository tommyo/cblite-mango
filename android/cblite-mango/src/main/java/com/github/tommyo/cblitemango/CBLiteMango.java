package com.github.tommyo.cblitemango;

import android.util.Log;

import com.couchbase.lite.ArrayExpression;
import com.couchbase.lite.ArrayFunction;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Select;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.VariableExpression;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class CBLiteMango {
    public static Query query(Database db, String req) throws Exception {
        return query(db, new JSONObject(req));
    }

    public static Query query(Database db, JSONObject req) throws Exception {
        return select(req)
                .from(DataSource.database(db))
                .where(where(req));
    }

    protected static Select select(JSONObject q) throws Exception {
        Log.i("Mango", q.toString(2));
        if (!q.has("fields")) {
            return QueryBuilder.select(SelectResult.all());
        }
        try {
            JSONArray req = q.getJSONArray("fields");
            Log.i("Mango", req.toString(2) + "(" + req.length() + ")");
            SelectResult[] fields = new SelectResult[req.length()];
            for (int i = 0; i < req.length(); i++) {
                String prop = req.getString(i);
                switch (prop) {
                    case "_id":
                        Log.i("Mango", "Adding _id to selectors");
                        fields[i] = SelectResult.expression(Meta.id).as("_id");
                        break;
                    case "_rev":
                        Log.i("Mango", "Adding _rev to selectors");
                        fields[i] = SelectResult.expression(Meta.sequence).as("_rev");
                        break;
                    default:
                        Log.i("Mango", "Adding generic " + prop + " to selectors");
                        fields[i] = SelectResult.property(prop);
                        break;
                }
            }
            return QueryBuilder.select(fields);
        } catch (JSONException e) {
            // e.printStackTrace();
            throw(new Exception("Error parsing fields array"));
        }
    }

    public static Expression where(JSONObject q) throws Exception {
        return parse(q.getJSONObject("selector"));
    }

    protected static Expression parseValue(String prop, Object val) throws Exception {
        if (!(val instanceof JSONObject)) {
            // implicit $eq
            return eq(prop, val);
        }
        JSONArray keys = ((JSONObject) val).names();
        for (int i = 0; i < keys.length(); i++) {
            String key = keys.getString(i);
            // array types

            switch (key) {
                // simple types
                case "$eq":
                    return eq(prop, ((JSONObject) val).get(key));
                case "$lt":
                    return lt(prop, ((JSONObject) val).get(key));
                case "$lte":
                    return lte(prop, ((JSONObject) val).get(key));
                case "$ne":
                    return ne(prop, ((JSONObject) val).get(key));
                case "$gte":
                    return gte(prop, ((JSONObject) val).get(key));
                case "$gt":
                    return gt(prop, ((JSONObject) val).get(key));
                // array types
                case "$in":
                    return in(prop, ((JSONObject) val).getJSONArray(key));
                case "$nin":
                    return nin(prop, ((JSONObject) val).getJSONArray(key));
                case "$exists":
                    return exists(prop, ((JSONObject) val).getBoolean(key));
                case "$mod":
                    return mod(prop, ((JSONObject) val).getDouble(key));
                case "$regex":
                    return regex(prop, ((JSONObject) val).getString(key));
                case "$all":
                    return all(prop, ((JSONObject) val).getJSONArray(key));
                case "$elemMatch":
                    return elemMatch(prop, ((JSONObject) val).getJSONArray(key));
                case "$type":
                    throw new Exception("$type is not supported");
                case "$size":
                    return size(prop, ((JSONObject) val).getDouble(key));
            }
        }
        return parseForSelector(prop, val);
    }

    // parser that checks type and returns, no parent
    protected static Expression parseForSelector(String key, Object val) throws Exception {
        switch (key) {
            case "$and":
                return and((JSONArray) val);
            case "$or":
                return or((JSONArray) val);
            case "$nor":
                return nor((JSONArray) val);
            case "$not":
                return not((JSONObject) val);
            default:
                // key is a field name.
                return parseValue(key, val);
        }
    }

    // parser that creates a base expression, with an explicit $and
    // used by where: {}, $and: [{}], $or: [{}], $nor: [{}], $not: {}
    public static Expression parse(JSONObject src) throws Exception {
        JSONArray keys = src.names();
        String first = keys.getString(0);
        Expression out = parseForSelector(first, src.get(first));
        for (int i = 1; i < keys.length(); i++) {
            String row = keys.getString(i);
            out = out.and(parseForSelector(row, src.get(row)));
        }
        return out;
    }


    public static Expression and(JSONArray src) throws Exception {
        JSONObject first = src.getJSONObject(0);
        Expression out = parse(first);
        for (int i = 1; i < src.length(); i++) {
            out = out.and(parse(src.getJSONObject(i)));
        }
        return out;
    }

    public static Expression or(JSONArray src) throws Exception {
        JSONObject first = src.getJSONObject(0);
        Expression out = parse(first);
        for (int i = 1; i < src.length(); i++) {
            out = out.or(parse(src.getJSONObject(i)));
        }
        return out;
    }

    public static Expression nor(JSONArray src) throws Exception {
        return Expression.negated(or(src));
    }

    public static Expression not(JSONObject src) throws Exception {
        return Expression.not(parse(src));
    }

    protected static Expression lt(String prop, Object src) {
        return Expression.property(prop).lessThan(Expression.value(src));
    }

    protected static Expression lte(String prop, Object src) {
        return Expression.property(prop).lessThanOrEqualTo(Expression.value(src));
    }

    protected static Expression eq(String prop, Object src) {
        return Expression.property(prop).equalTo(Expression.value(src));
    }

    protected static Expression ne(String prop, Object src) {
        if (src == JSONObject.NULL) {
            return Expression.property(prop).notNullOrMissing();
        }
        return Expression.property(prop).notEqualTo(Expression.value(src));
    }

    protected static Expression gte(String prop, Object src) {
        return Expression.property(prop).greaterThanOrEqualTo(Expression.value(src));
    }

    protected static Expression gt(String prop, Object src) {
        if (src == JSONObject.NULL) {
            Log.i("Mango", prop + " Not Null");
            return Expression.property(prop).notNullOrMissing();
        }
        return Expression.property(prop).greaterThan(Expression.value(src));
    }

    protected static Expression exists(String prop, Boolean check) {
        Expression field = Expression.property(prop);
        return (check) ? field.notNullOrMissing() : field.isNullOrMissing();
    }

    protected static Expression in(String prop, JSONArray src) throws JSONException {
        Expression[] all = new Expression[src.length()];
        for (int i = 0; i < src.length(); i++) {
            all[i] = Expression.value(src.get(i));
        }
        return Expression.property(prop).in(all);
    }

    protected static Expression all(String prop, JSONArray src) throws JSONException {
        Expression[] all = new Expression[src.length()];
        for (int i = 0; i < src.length(); i++) {
            all[i] = Expression.value(src.get(i));
        }

        VariableExpression m = ArrayExpression.variable("__all_" + prop + "__");

        return ArrayExpression.every(m).in(Expression.property(prop)).satisfies(m.in(all));
    }

    protected static Expression elemMatch(String prop, JSONArray src) throws JSONException {
        Expression[] all = new Expression[src.length()];
        for (int i = 0; i < src.length(); i++) {
            all[i] = Expression.value(src.get(i));
        }

        VariableExpression m = ArrayExpression.variable("__elem_match_" + prop + "__");

        return ArrayExpression.any(m).in(Expression.property(prop)).satisfies(m.in(all));
    }

    protected static Expression nin(String prop, JSONArray src) throws JSONException {
        return Expression.not(in(prop, src));
    }

    protected static Expression mod(String prop, Double n) {
        return Expression.property(prop).modulo(Expression.doubleValue(n));
    }

    protected static Expression regex(String prop, String pattern) {
        return Expression.property(prop).regex(Expression.string(pattern));
    }

    protected static Expression size(String prop, Double len) {
        return ArrayFunction.length(Expression.property(prop)).equalTo(Expression.number(len));
    }
}