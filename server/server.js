const express = require("express");
const crypto = require("crypto");
const app = express();
app.use(express.json({ limit: "16kb" }));

// RAM-only relay. Messages are never persisted to disk.
const inbox = new Map();
const MAX_PER_INBOX = 200;
const TTL_MS = 24 * 60 * 60 * 1000;
const validId = value => typeof value === "string" && /^[A-Za-z0-9_-]{8,80}$/.test(value);

app.get("/health", (_req, res) => res.json({ ok: true, service: "FaceConnect relay" }));

app.post("/messages", (req, res) => {
  const { from, to, body } = req.body || {};
  if (!validId(from) || !validId(to) || typeof body !== "string" || !body.trim() || body.length > 4000)
    return res.status(400).json({ error: "Invalid message" });
  if (from === to) return res.status(400).json({ error: "Cannot message yourself" });
  const now = Date.now();
  const list = (inbox.get(to) || []).filter(m => now - m.sentAt < TTL_MS);
  if (list.length >= MAX_PER_INBOX) return res.status(429).json({ error: "Inbox full" });
  list.push({ id: crypto.randomUUID(), from, to, body: body.trim(), sentAt: now });
  inbox.set(to, list);
  return res.status(202).json({ accepted: true });
});

app.get("/inbox/:uid", (req, res) => {
  if (!validId(req.params.uid)) return res.status(400).json({ error: "Invalid ID" });
  const now = Date.now();
  const messages = (inbox.get(req.params.uid) || []).filter(m => now - m.sentAt < TTL_MS);
  inbox.delete(req.params.uid);
  return res.json({ messages });
});

setInterval(() => {
  const now = Date.now();
  for (const [uid, list] of inbox) {
    const fresh = list.filter(m => now - m.sentAt < TTL_MS);
    if (fresh.length) inbox.set(uid, fresh); else inbox.delete(uid);
  }
}, 10 *  60 * 1000).unref();

const port = process.env.PORT || 3000;
app.listen(port, "0.0.0.0", () => console.log("FaceConnect relay on " + port));
