const express=require("express"),crypto=require("crypto"),bcrypt=require("bcryptjs"),jwt=require("jsonwebtoken"),{Pool}=require("pg");
const app=express();app.use(express.json({limit:"16kb"}));
const pool=new Pool({connectionString:process.env.DATABASE_URL,ssl:process.env.DATABASE_URL?{rejectUnauthorized:false}:false});
const secret=process.env.JWT_SECRET;
if(!secret)throw new Error("Set JWT_SECRET in Render environment variables");
let ready=pool.query(`CREATE TABLE IF NOT EXISTS users(uid VARCHAR(32) PRIMARY KEY,name VARCHAR(60) NOT NULL,email VARCHAR(254) UNIQUE NOT NULL,password_hash TEXT NOT NULL,created_at BIGINT NOT NULL)`);
const sign=u=>jwt.sign({uid:u.uid,email:u.email},secret,{expiresIn:"30d"});
const safe=u=>({uid:u.uid,name:u.name,email:u.email,token:sign(u)});
app.get("/health",async(_q,r)=>{try{await ready;await pool.query("SELECT 1");r.json({ok:true,service:"FaceConnect API"});}catch(e){r.status(503).json({ok:false});}});
app.post("/auth/register",async(q,r)=>{
 try{await ready;const name=String(q.body.name||"").trim(),email=String(q.body.email||"").trim().toLowerCase(),password=String(q.body.password||"");
 if(name.length<2||name.length>60)return r.status(400).json({error:"Name 2-60 characters hona chahiye"});
 if(!/^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$/.test(email))return r.status(400).json({error:"Valid email required"});
 if(password.length<8||password.length>100)return r.status(400).json({error:"Password 8-100 characters hona chahiye"});
 const hash=await bcrypt.hash(password,12),uid=crypto.randomBytes(16).toString("hex");
 const result=await pool.query("INSERT INTO users(uid,name,email,password_hash,created_at) VALUES($1,$2,$3,$4,$5) RETURNING uid,name,email",[uid,name,email,hash,Date.now()]);
 r.status(201).json(safe(result.rows[0]));
 }catch(e){if(e.code==="23505")return r.status(409).json({error:"Email already registered. Login karein"});r.status(500).json({error:"Registration failed"});}
});
app.post("/auth/login",async(q,r)=>{
 try{await ready;const email=String(q.body.email||"").trim().toLowerCase(),password=String(q.body.password||"");
 const result=await pool.query("SELECT uid,name,email,password_hash FROM users WHERE email=$1",[email]),u=result.rows[0];
 if(!u||!(await bcrypt.compare(password,u.password_hash)))return r.status(401).json({error:"Email ya password galat hai"});
 r.json(safe(u));
 }catch(e){r.status(500).json({error:"Login failed"});}
});
const inbox=new Map(),TTL=24*60*60*1000,validId=v=>typeof v==="string"&&/^[a-f0-9]{32}$/.test(v);
app.post("/messages",(q,r)=>{const{from,to,body}=q.body||{};if(!validId(from)||!validId(to)||typeof body!=="string"||!body.trim()||body.length>4000)return r.status(400).json({error:"Invalid message"});const list=(inbox.get(to)||[]).filter(m=>Date.now()-m.sentAt<TTL);if(list.length>=200)return r.status(429).json({error:"Inbox full"});list.push({id:crypto.randomUUID(),from,to,body:body.trim(),sentAt:Date.now()});inbox.set(to,list);r.status(202).json({accepted:true});});
app.get("/inbox/:uid",(q,r)=>{if(!validId(q.params.uid))return r.status(400).json({error:"Invalid ID"});const messages=(inbox.get(q.params.uid)||[]).filter(m=>Date.now()-m.sentAt<TTL);inbox.delete(q.params.uid);r.json({messages});});
setInterval(()=>{for(const[id,list]of inbox){const keep=list.filter(m=>Date.now()-m.sentAt<TTL);if(keep.length)inbox.set(id,keep);else inbox.delete(id);}},600000).unref();
const port=process.env.PORT||3000;app.listen(port,"0.0.0.0",()=>console.log("FaceConnect API on "+port));
