import express from "express";
import "express-async-errors";
import { json } from "body-parser";

import {
  currentUser,
  errorHandler,
  NotFoundError,
} from "@handev-ticketing/common";
import cookieSession from "cookie-session";
import { createTicketRouter } from "./routes/new";

const app = express();
app.set("trust proxy", true);
app.use(json());
app.use(
  cookieSession({
    signed: false,
    secure: false,
  })
);

app.use(currentUser);

app.use("/api/tickets", createTicketRouter);

app.all("*", (req, res) => {
  throw new NotFoundError();
});

app.use(errorHandler);

export { app };
