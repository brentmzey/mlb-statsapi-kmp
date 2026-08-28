/**
 * Runnable Node.js Application Demo for MLB-StatsAPI.
 */
const https = require('https');

function fetchJson(url) {
    return new Promise((resolve, reject) => {
        https.get(url, { headers: { 'User-Agent': 'mlb-statsapi-node-demo' } }, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(data));
                } catch (e) {
                    reject(e);
                }
            });
        }).on('error', reject);
    });
}

async function runDemo() {
    console.log("==============================================================================");
    console.log(" 🌐 MLB-StatsAPI JavaScript / Node.js Native Demo");
    console.log("==============================================================================");

    // 1. Live Schedule
    console.log("\n📅 1. Fetching Live MLB Schedule...");
    const schedule = await fetchJson('https://statsapi.mlb.com/api/v1/schedule?sportId=1');
    const games = schedule.dates?.[0]?.games || [];
    console.log(`✅ Loaded ${games.length} scheduled games for ${schedule.dates?.[0]?.date || 'today'}:`);
    games.slice(0, 3).forEach(g => {
        console.log(`   • ${g.teams.away.team.name} @ ${g.teams.home.team.name} (${g.status.detailedState})`);
    });

    // 2. Standings & Functional Stream Filter
    console.log("\n📊 2. Fetching Standings (2026 Season)...");
    const standingsData = await fetchJson('https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season=2026');
    const records = standingsData.records || [];
    
    // Functional pipeline
    const divisionLeaders = records.map(rec => {
        const topTeam = rec.teamRecords[0];
        return {
            division: rec.division?.name || 'Division',
            team: topTeam.team.name,
            record: `${topTeam.wins}-${topTeam.losses}`,
            diff: topTeam.runDifferential
        };
    });

    console.log("✅ Division Leaders via JavaScript functional map/filter:");
    divisionLeaders.forEach(l => {
        console.log(`   • ${l.division.padEnd(30)}: ${l.team.padEnd(24)} ${l.record} (Diff: %+d)`.replace('%+d', l.diff > 0 ? `+${l.diff}` : l.diff));
    });

    console.log("\n==============================================================================");
    console.log(" ✅ Node.js Demo Execution Completed Successfully!");
    console.log("==============================================================================");
}

runDemo().catch(console.error);
