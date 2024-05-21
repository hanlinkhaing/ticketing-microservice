import express from "express";

const router = express.Router();

router.get("/currentuser", (req, res) => {
  res.send("Hi there!");
});

export { router as currentUserRouter };
