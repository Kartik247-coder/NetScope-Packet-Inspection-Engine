import express from "express";
import { getFlows } from "../controllers/flowController.js";

const router = express.Router();

router.get("/flows", getFlows);

export default router;

