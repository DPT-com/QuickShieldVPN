from fastapi import FastAPI

app = FastAPI(title="QuickShield VPN API")

SERVERS = [
    {
        "id": "de-1",
        "country": "Germany",
        "city": "Frankfurt",
        "enabled": False,
    },
    {
        "id": "us-1",
        "country": "United States",
        "city": "New York",
        "enabled": False,
    },
    {
        "id": "sg-1",
        "country": "Singapore",
        "city": "Singapore",
        "enabled": False,
    },
]

@app.get("/")
def home():
    return {"app": "QuickShield VPN", "status": "development"}

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/v1/servers")
def list_servers():
    return [
        {
            "id": s["id"],
            "country": s["country"],
            "city": s["city"],
        }
        for s in SERVERS if s["enabled"]
    ]
