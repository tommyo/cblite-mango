import Foundation
import CouchbaseLiteSwift

public func buildQuery(req: Data) throws -> Query {
    let json = try JSONSerialization.jsonObject(with: req, options: [])
    let data = json as! [String: Any]
    return try buildQuery(req: data)
}

public func buildQuery(req: [String: Any]) throws -> Query {
    return try select(req: req);
}

private func select(req: [String: Any]) throws -> Select {
    //if req["fields"] {
        return QueryBuilder.select(SelectResult.all())
    //}
    
}
