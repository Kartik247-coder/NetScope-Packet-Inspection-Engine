import axios from "axios";

const engineBase = process.env.DPI_ENGINE_URL || "http://localhost:8080";

const client = axios.create({
  baseURL: engineBase,
  timeout: 2500
});

export async function getStats(req, res) {
  try {
    const r = await client.get("/stats");
    res.json(r.data);
  } catch (e) {
    res.status(502).json({
      error: e?.message || "Failed to fetch /stats from engine",
      engineBase
    });
  }
}

