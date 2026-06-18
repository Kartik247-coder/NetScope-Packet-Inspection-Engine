import axios from "axios";

const client = axios.create({
  baseURL: "/api",
  timeout: 2500
});

export async function getStats() {
  const r = await client.get("/stats");
  return r.data;
}

export async function getFlows() {
  const r = await client.get("/flows");
  return r.data;
}

