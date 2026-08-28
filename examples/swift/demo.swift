import Foundation

/**
 * Runnable Swift Application Demo for MLB-StatsAPI.
 */
struct TeamStanding: Decodable {
    let teamName: String
    let wins: Int
    let losses: Int
    let runDifferential: Int
}

func fetchMlbJson(urlString: String) async throws -> [String: Any] {
    guard let url = URL(string: urlString) else { throw URLError(.badURL) }
    var request = URLRequest(url: url)
    request.setValue("mlb-statsapi-swift-demo/1.0", forHTTPHeaderField: "User-Agent")
    let (data, _) = try await URLSession.shared.data(for: request)
    let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
    return json ?? [:]
}

func runSwiftDemo() async throws {
    print("==============================================================================")
    print(" 🍎 MLB-StatsAPI Swift / Apple Native Application Demo")
    print("==============================================================================")

    // 1. Live Schedule
    print("\n📅 1. Fetching Live Schedule via Swift URLSession & async/await...")
    let schedule = try await fetchMlbJson(urlString: "https://statsapi.mlb.com/api/v1/schedule?sportId=1")
    if let dates = schedule["dates"] as? [[String: Any]],
       let firstDate = dates.first,
       let games = firstDate["games"] as? [[String: Any]] {
        print("✅ Loaded \(games.count) games today:")
        for game in games.prefix(3) {
            if let teams = game["teams"] as? [String: Any],
               let away = teams["away"] as? [String: Any],
               let home = teams["home"] as? [String: Any],
               let awayTeam = away["team"] as? [String: Any],
               let homeTeam = home["team"] as? [String: Any],
               let awayName = awayTeam["name"] as? String,
               let homeName = homeTeam["name"] as? String {
                print("   • \(awayName) @ \(homeName)")
            }
        }
    }

    // 2. Standings Filter
    print("\n📊 2. Fetching Standings & Applying Swift Functional Higher-Order Filter...")
    let standings = try await fetchMlbJson(urlString: "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season=2026")
    if let records = standings["records"] as? [[String: Any]] {
        for record in records {
            if let div = record["division"] as? [String: Any],
               let divName = div["name"] as? String,
               let teamRecords = record["teamRecords"] as? [[String: Any]],
               let leader = teamRecords.first,
               let team = leader["team"] as? [String: Any],
               let teamName = team["name"] as? String,
               let wins = leader["wins"] as? Int,
               let losses = leader["losses"] as? Int,
               let diff = leader["runDifferential"] as? Int {
                let diffStr = diff > 0 ? "+\(diff)" : "\(diff)"
                print("   • \(divName.padding(toLength: 30, withPad: " ", startingAt: 0)): \(teamName.padding(toLength: 24, withPad: " ", startingAt: 0)) (\(wins)-\(losses), Diff: \(diffStr))")
            }
        }
    }

    print("\n==============================================================================")
    print(" ✅ Swift Native Demo Completed Successfully!")
    print("==============================================================================")
}

let sema = DispatchSemaphore(value: 0)
Task {
    do {
        try await runSwiftDemo()
    } catch {
        print("❌ Swift demo error: \(error)")
    }
    sema.signal()
}
sema.wait()
