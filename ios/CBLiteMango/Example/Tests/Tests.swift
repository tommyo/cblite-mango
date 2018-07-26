// https://github.com/Quick/Quick

import Quick
import Nimble
import CBLiteMango
import CouchbaseLiteSwift

class TableOfContentsSpec: QuickSpec {
    override func spec() {
        var db: Database!
        
        beforeSuite {
            do {
                db = try Database(name: "mango_test_db")
                try addRecords()
                print("Records:", db.count)
            } catch {
                fatalError("Problem initializing database")
            }
        }
        
        afterSuite {
            try? db.delete()
        }
        
        func addRecords() throws {
            try db.saveDocument(MutableDocument(id: "mario")
                .setString("mario", forKey: "name")
                .setInt(5, forKey: "rank")
                .setString("mario", forKey:"series")
                .setInt(1981, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "puff")
                .setString("jigglypuff", forKey: "name")
                .setInt(8, forKey: "rank")
                .setString("pokemon", forKey:"series")
                .setInt(1996, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "link")
                .setString("link", forKey: "name")
                .setInt(10, forKey: "rank")
                .setString("zelda", forKey:"series")
                .setInt(1986, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "dk")
                .setString("donkey kong", forKey: "name")
                .setInt(7, forKey: "rank")
                .setString("mario", forKey:"series")
                .setInt(1981, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "pikachu")
                .setString("pikachu", forKey: "name")
                .setInt(1, forKey: "rank")
                .setString("pokemon", forKey:"series")
                .setInt(1996, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "falcon")
                .setString("captain falcon", forKey: "name")
                .setInt(4, forKey: "rank")
                .setString("f-zero", forKey:"series")
                .setInt(1990, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "luigi")
                .setString("luigi", forKey: "name")
                .setInt(11, forKey: "rank")
                .setString("mario", forKey:"series")
                .setInt(1983, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "fox")
                .setString("fox", forKey: "name")
                .setInt(3, forKey: "rank")
                .setString("star fox", forKey:"series")
                .setInt(1993, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "ness")
                .setString("ness", forKey: "name")
                .setInt(9, forKey: "rank")
                .setString("earthbound", forKey:"series")
                .setInt(1994, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "samus")
                .setString("samus", forKey: "name")
                .setInt(12, forKey: "rank")
                .setString("metroid", forKey:"series")
                .setInt(1986, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "yoshi")
                .setString("yoshi", forKey: "name")
                .setInt(6, forKey: "rank")
                .setString("mario", forKey:"series")
                .setInt(1990, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "kirby")
                .setString("kirby", forKey: "name")
                .setInt(2, forKey: "rank")
                .setString("kirby", forKey:"series")
                .setInt(1992, forKey:"debut"))
            
            try db.saveDocument(MutableDocument(id: "james")
                .setString("James", forKey: "name")
                .setInt(20, forKey: "age")
                .setArray(MutableArrayObject(data:["mario", "pokemon"]), forKey: "favorites"))
            
            try db.saveDocument(MutableDocument(id: "mary")
                .setString("Mary", forKey: "name")
                .setInt(21, forKey: "age")
                .setArray(MutableArrayObject(data:["pokemon"]), forKey: "favorites"))
            
            try db.saveDocument(MutableDocument(id: "william")
                .setString("William", forKey: "name")
                .setInt(23, forKey: "age")
                .setArray(MutableArrayObject(data:["mario"]), forKey: "favorites"))
        }
        
        describe("setup") {
            it("has the records it needs") {
                expect(15) == db.count
            }
        }

        describe("a query") {
            
        }
        
        describe("these will fail") {

            it("can do maths") {
                expect(1) == 2
            }

            it("can read") {
                expect("number") == "string"
            }

            it("will eventually fail") {
                expect("time").toEventually( equal("done") )
            }
            
            context("these will pass") {

                it("can do maths") {
                    expect(23) == 23
                }

                it("can read") {
                    expect("🐮") == "🐮"
                }

                it("will eventually pass") {
                    var time = "passing"

                    DispatchQueue.main.async {
                        time = "done"
                    }

                    waitUntil { done in
                        Thread.sleep(forTimeInterval: 0.5)
                        expect(time) == "done"

                        done()
                    }
                }
            }
        }
    }
}
