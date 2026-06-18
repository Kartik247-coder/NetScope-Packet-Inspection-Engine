import express from "express";
import cors from "cors";
import statsRouter from "./routes/stats.js";
import flowsRouter from "./routes/flows.js";

const app = express();
app.use(cors());
app.use(express.json());

app.get("/health", (req, res) => res.json({ ok: true }));
app.use("/api", statsRouter);
app.use("/api", flowsRouter);

const port = process.env.PORT ? Number(process.env.PORT) : 3001;
app.listen(port, () => {
  // eslint-disable-next-line no-console
  console.log(`Dashboard backend listening on http://localhost:${port}`);
  // eslint-disable-next-line no-console
  console.log(`Proxying Java engine at ${process.env.DPI_ENGINE_URL || "http://localhost:8080"}`);
});

