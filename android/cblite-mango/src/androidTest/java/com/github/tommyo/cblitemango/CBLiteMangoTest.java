package com.github.tommyo.cblitemango;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.couchbase.lite.Array;
import com.couchbase.lite.ArrayExpression;
import com.couchbase.lite.ArrayFunction;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.VariableExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CBLiteMangoTest {

    private Database db;

    private long totalRecordCount;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        DatabaseConfiguration config = new DatabaseConfiguration(context);
        db = new Database("mango_test_db", config);

        addRecords();
        totalRecordCount = db.getCount();
        Log.i("Mango", "Records: " + totalRecordCount);
    }

    @After
    public void tearDown() throws Exception {
        db.delete();
    }

    @Test
    public void implicitEq() throws Exception {
        String selector = "{ \"_id\": \"pikachu\" }";
        String fields = "[ \"_id\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").equalTo(Expression.string("pikachu")));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row: r) {
            assertEquals("pikachu", row.getString("_id"));
            found++;
        }
        assertEquals(1, found);
    }

    @Test
    public void explicitEq() throws Exception {
        String selector = "{ \"_id\": { \"$eq\": \"pikachu\" } }";
        String fields = "[ \"_id\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").equalTo(Expression.string("pikachu")));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row: r) {
            assertEquals("pikachu", row.getString("_id"));
            found++;
        }
        assertEquals(1, found);
    }

    @Test
    public void not() throws Exception {
        String selector = "{ \"$not\": { \"_id\": \"pikachu\" } }";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.negated(Expression.property("_id").equalTo(Expression.string("pikachu"))));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row: r) {
            assertNotEquals("pikachu", row.getString("_id"));
            found++;
        }
        assertEquals(totalRecordCount - 1, found);
    }

    @Test
    public void implicitAnd() throws Exception {
        String selector = "{ \"_id\": \"pikachu\", \"rank\": 1 }";
        String fields = "[\"_id\", \"rank\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.property("rank"))
                .from(DataSource.database(db))
                .where(Expression.property("_id").equalTo(Expression.string("pikachu"))
                        .and(Expression.property("rank").equalTo(Expression.intValue(1))));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row: r) {
            assertEquals("pikachu", row.getString("_id"));
            assertEquals(1, row.getLong("rank"));
            found++;
        }
        assertEquals(1, found);
    }

    @Test
    public void explicitAnd() throws Exception {
        String selector = "{" +
                "\"$and\": [" +
                "{ \"_id\": \"pikachu\" }," +
                "{ \"rank\": 1 }" +
                "]" +
                "}";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").equalTo(Expression.string("pikachu"))
                        .and(Expression.property("rank").equalTo(Expression.intValue(1)))
                );

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row: r) {
            assertEquals("pikachu", row.getString("_id"));
            found++;
        }
        assertEquals(1, found);
    }

    @Test
    public void or() throws Exception {
        String selector = "{" +
                "\"$or\": [" +
                "{ \"_id\": \"mario\" }," +
                "{ \"_id\": \"luigi\" }" +
                "]" +
                "}";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").equalTo(Expression.string("mario"))
                        .or(Expression.property("_id").equalTo(Expression.string("luigi"))));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row: r) {
            assertThat(row.getString("_id"), anyOf(is("mario"), is("luigi")));
            found++;
        }
        assertEquals(2, found);
    }

    @Test
    public void nor() throws Exception {
        String selector = "{" +
                "\"_id\": { \"$gt\": null }," +
                "\"$nor\": [" +
                "{ \"_id\": \"mario\" }," +
                "{ \"_id\": \"luigi\" }" +
                "]" +
                "}";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").notNullOrMissing()
                        .and(
                                Expression.negated(Expression.property("_id").equalTo(Expression.string("mario"))
                                        .or(Expression.property("_id").equalTo(Expression.string("luigi"))))
                        ));

        assertEquals(expected.explain(), got.explain());

        Log.i("Mango", expected.explain());

        long found = 0;
        ResultSet r = expected.execute();
        for (Result row : r) {
            String id = row.getString("_id");
            assertNotEquals("mario", id);
            assertNotEquals("luigi", id);
        }
        assertEquals(totalRecordCount - 2, found);
    }

    @Test
    public void in() throws Exception {
        String selector = "{ \"_id\": { \"$in\": [ \"pikachu\", \"puff\" ] } }";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").in(Expression.string("pikachu"), Expression.string("puff")));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row: r) {
            assertThat(row.getString("_id"), anyOf(is("pikachu"), is("puff")));
            found++;
        }
        assertEquals(2, found);
    }

    @Test
    public void nin() throws Exception {
        String selector = "{ \"_id\": { \"$nin\": [ \"pikachu\", \"puff\" ] } }";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.negated(Expression.property("_id").in(Expression.string("pikachu"), Expression.string("puff"))));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row : r) {
            String id = row.getString("_id");
            assertNotEquals("pikachu", id);
            assertNotEquals("puff", id);
            found++;
        }
        assertEquals(totalRecordCount - 2, found);
    }

    @Test
    public void all() throws Exception {
        String selector = "{ \"favorites\": { \"$all\": [ \"mario\", \"pokemon\" ] } }";
        String fields = "[ \"_id\", \"favorites\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        VariableExpression m = ArrayExpression.variable("__all_favorites__");
        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.property("favorites"))
                .from(DataSource.database(db))
                .where(
                        ArrayExpression.every(m)
                                .in(Expression.property("favorites"))
                                .satisfies(m.in(Expression.string("mario"), Expression.string("pokemon"))
                                ));

        // assertEquals(expected.explain(), got.explain());

        Log.i("Mango", expected.explain());

        long found = 0;
        ResultSet r = expected.execute();
        for (Result row: r) {
            Array favorites = row.getArray("favorites");
            if (favorites != null) for (Object k : favorites.toList()) {
                Log.i("Mango", row.getString("_id") +" favorite: " + k);
            }

//            assertThat(row.getArray("favorites").toList(), hasItem("mario"));
//            assertThat(row.getArray("favorites").toList(), hasItem("pokemon"));
            found++;
        }
        assertEquals(1, found);
    }

    @Test
    public void elemMatch() throws Exception {
        String selector = "{ \"favorites\": { \"$elemMatch\": [ \"mario\" ] } }";
        String fields = "[ \"_id\", \"favorites\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        VariableExpression m = ArrayExpression.variable("__elem_match_favorites__");
        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.property("favorites"))
                .from(DataSource.database(db))
                .where(
                        ArrayExpression.any(m)
                                .in(Expression.property("favorites"))
                                .satisfies(m.in(Expression.string("mario"))
                                ));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row: r) {
            assertThat(row.getArray("favorites").toList(), hasItem("mario"));
            found++;
        }
        assertEquals(2, found);
    }


    @Test
    public void exists() throws Exception {
        String selector = "{ \"rank\": { \"$exists\": true } }";
        String fields = "[ \"_id\", \"rank\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.property("rank"))
                .from(DataSource.database(db))
                .where(Expression.property("rank").notNullOrMissing());

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row : r) {
            assertNotNull(row.getValue("rank"));
            found++;
        }
        assertEquals(12, found);
    }

    @Test
    public void gt() throws Exception {
        String selector = "{ \"_id\": { \"$gt\": \"m\" } }";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").greaterThan(Expression.string("m")));

        assertEquals(expected.explain(), got.explain());

        ResultSet r = got.execute();
        for (Result row : r) {
            assertThat(row.getString("_id"), greaterThan("m"));
        }
    }

    @Test
    public void lte() throws Exception {
        String selector = "{ \"_id\": { \"$lte\": \"luigi\" } }";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").lessThanOrEqualTo(Expression.string("luigi")));

        assertEquals(expected.explain(), got.explain());

        ResultSet r = got.execute();
        for (Result row : r) {
            assertThat(row.getString("_id"), lessThanOrEqualTo("luigi"));
        }
    }

    @Test
    public void gtNull() throws Exception {
        String selector = "{ \"_id\": { \"$gt\": null } }";
        String fields = "[\"_id\"]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(db))
                .where(Expression.property("_id").notNullOrMissing());

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row : r) {
            found++;
            assertNotNull(row.getValue("_id"));
        }
        assertEquals(totalRecordCount, found);
    }

    @Test
    public void size() throws Exception {
        String selector = "{ \"favorites\": { \"$size\": 2 } }";
        String fields = "[ \"_id\", \"favorites\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.property("favorites"))
                .from(DataSource.database(db))
                .where(ArrayFunction.length(Expression.property("favorites")).equalTo(Expression.number(2)));

        assertEquals(expected.explain(), got.explain());

        ResultSet r = got.execute();
        for (Result row : r) {
            assertEquals(row.getArray("favorites").count(), 2);
        }
    }

    @Test
    public void inArray() throws Exception {
        String selector = "{ \"favorites\": { \"$in\": [ \"mario\" ] } }";
        String fields = "[ \"_id\", \"favorites\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.property("favorites"))
                .from(DataSource.database(db))
                .where(Expression.property("favorites").in(Expression.string("mario")));

 /*
        Query expected = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("favorites"))
            .from(DataSource.database(db))
            .where(Expression.property("favorites").in(Expression.string("mario"))
                .or(ArrayFunction.contains(Expression.property("favorites"), Expression.string("mario"))));
*/

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row : r) {
            assertThat(row.getArray("favorites").toList(), hasItem("mario"));
            found++;
        }
        assertEquals(2, found);
    }

    // more complex tests

    @Test
    public void inAndLt() throws Exception {
        String selector = "{" +
                "\"$and\": [" +
                "{ \"_id\": { \"$in\": [ \"pikachu\", \"puff\" ] } }," +
                "{ \"rank\": { \"$lt\": 8 } }" +
                "]" +
                "}";
        String fields = "[ \"_id\", \"rank\" ]";
        Query got = CBLiteMango.query(db, String.format("{ \"selector\": %s, \"fields\": %s }", selector, fields));

        Query expected = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.property("rank"))
                .from(DataSource.database(db))
                .where(Expression.property("_id").in(Expression.string("pikachu"), Expression.string("puff"))
                        .and(Expression.property("rank").lessThan(Expression.number(8))));

        assertEquals(expected.explain(), got.explain());

        long found = 0;
        ResultSet r = got.execute();
        for (Result row : r) {
            found++;
            assertThat(row.getString("_id"), anyOf(is("pikachu"), is("puff")));
            assertNotNull(row.getValue("rank"));
            assertThat(row.getInt("rank"), lessThan(8));
        }
        assertEquals(1, found);

    }

    private void addRecords() throws Exception {

        // { name: 'mario', _id: 'mario', rank: 5, series: 'mario', debut: 1981 },

        db.save(new MutableDocument("mario", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "mario");
            put("rank", 5);
            put("series", "mario");
            put("debut", 1981);
        }})));

        // { name: 'jigglypuff', _id: 'puff', rank: 8, series: 'pokemon', debut: 1996 },

        db.save(new MutableDocument("puff", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "jigglypuff");
            put("rank", 8);
            put("series", "pokemon");
            put("debut", 1996);
        }})));

        // { name: 'link', rank: 10, _id: 'link', series: 'zelda', debut: 1986 },

        db.save(new MutableDocument("link", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "link");
            put("rank", 10);
            put("series", "zelda");
            put("debut", 1986);
        }})));

        // { name: 'donkey kong', rank: 7, _id: 'dk', series: 'mario', debut: 1981 },

        db.save(new MutableDocument("dk", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "donkey kong");
            put("rank", 7);
            put("series", "mario");
            put("debut", 1981);
        }})));

        // { name: 'pikachu', series: 'pokemon', _id: 'pikachu', rank: 1, debut: 1996 },

        db.save(new MutableDocument("pikachu", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "pikachu");
            put("rank", 1);
            put("series", "pokemon");
            put("debut", 1996);
        }})));

        // { name: 'captain falcon', _id: 'falcon', rank: 4, series: 'f-zero', debut: 1990 },

        db.save(new MutableDocument("falcon", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "captain falcon");
            put("rank", 4);
            put("series", "f-zero");
            put("debut", 1990);
        }})));

        // { name: 'luigi', rank: 11, _id: 'luigi', series: 'mario', debut: 1983 },

        db.save(new MutableDocument("luigi", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "luigi");
            put("rank", 11);
            put("series", "mario");
            put("debut", 1983);
        }})));

        // { name: 'fox', _id: 'fox', rank: 3, series: 'star fox', debut: 1993 },

        db.save(new MutableDocument("fox", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "fox");
            put("rank", 3);
            put("series", "star fox");
            put("debut", 1993);
        }})));

        // { name: 'ness', rank: 9, _id: 'ness', series: 'earthbound', debut: 1994 },

        db.save(new MutableDocument("ness", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "ness");
            put("rank", 9);
            put("series", "earthbound");
            put("debut", 1994);
        }})));

        // { name: 'samus', rank: 12, _id: 'samus', series: 'metroid', debut: 1986 },

        db.save(new MutableDocument("samus", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "samus");
            put("rank", 12);
            put("series", "metroid");
            put("debut", 1986);
        }})));

        // { name: 'yoshi', _id: 'yoshi', rank: 6, series: 'mario', debut: 1990 },

        db.save(new MutableDocument("yoshi", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "yoshi");
            put("rank", 6);
            put("series", "mario");
            put("debut", 1990);
        }})));

        // { name: 'kirby', _id: 'kirby', series: 'kirby', rank: 2, debut: 1992 },

        db.save(new MutableDocument("kirby", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "kirby");
            put("rank", 2);
            put("series", "kirby");
            put("debut", 1992);
        }})));

        // { name: 'James', _id: 'james',  favorites: ['Mario', 'Pokemon'], age: 20 },

        db.save(new MutableDocument("james", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "James");
            put("age", 20);
            put("favorites", new MutableArray(Arrays.asList(new Object[]{ "mario", "pokemon" })));
        }})));

        // { name: 'Mary', _id: 'mary',  favorites: ['Pokemon'], age: 21 },

        db.save(new MutableDocument("mary", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "Mary");
            put("age", 21);
            put("favorites", new MutableArray(Arrays.asList(new Object[]{ "pokemon" })));
        }})));

        // { name: 'William', _id: 'william', favorites: ['Mario'], age: 23}

        db.save(new MutableDocument("william", Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", "William");
            put("age", 23);
            put("favorites", new MutableArray(Arrays.asList(new Object[]{ "mario" })));
        }})));
    }
}