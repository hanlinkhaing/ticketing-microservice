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
import { showTicketRouter } from "./routes/show";
import { indexTicketRouter } from "./routes";
import { updateTicketRouter } from "./routes/update";

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
app.use("/api/tickets", showTicketRouter);
app.use("/api/tickets", indexTicketRouter);
app.use("/api/tickets", updateTicketRouter);

app.all("*", (req, res) => {
  throw new NotFoundError();
});

app.use(errorHandler);

export { app };
