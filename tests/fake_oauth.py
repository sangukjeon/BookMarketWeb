"""테스트용 가짜 OAuth 서버 (카카오·네이버·구글의 주소와 응답 모양을 흉내). 포트 8097.
POST /_next  {"provider":"kakao","id":"..","name":"..","email":"..","deny":false,"badcode":false}
  → 다음 authorize 요청에서 돌려줄 사용자를 정한다 (실제 서버의 '로그인·동의' 단계 대신)."""
import json, secrets, urllib.parse
from http.server import BaseHTTPRequestHandler, HTTPServer

CB = "http://localhost:8080/BookMarketWeb/oauth/{}/callback"
CLIENT = {"kakao": ("KAKAO_ID", ""), "naver": ("NAVER_ID", "NAVER_SECRET"), "google": ("GOOGLE_ID", "GOOGLE_SECRET")}
NEXT = {}; CODES = {}; TOKENS = {}

class H(BaseHTTPRequestHandler):
    def log_message(self, *a): pass
    def send(self, code, body, ctype="application/json", headers=None):
        b = body.encode() if isinstance(body, str) else body
        self.send_response(code); self.send_header("Content-Type", ctype); self.send_header("Content-Length", str(len(b)))
        for k, v in (headers or {}).items(): self.send_header(k, v)
        self.end_headers(); self.wfile.write(b)
    def redirect(self, url): self.send(302, "", headers={"Location": url})

    def do_GET(self):
        u = urllib.parse.urlparse(self.path); q = dict(urllib.parse.parse_qsl(u.query)); path = u.path
        prov = path.split("/")[1]
        if path in ("/kakao/oauth/authorize", "/naver/oauth2.0/authorize", "/google/auth"):
            if q.get("client_id") != CLIENT[prov][0] or q.get("response_type") != "code": return self.send(400, '{"error":"invalid_client"}')
            if q.get("redirect_uri") != CB.format(prov): return self.send(400, '{"error":"redirect_uri_mismatch"}')
            if prov == "google" and "openid" not in q.get("scope", ""): return self.send(400, '{"error":"invalid_scope"}')
            user = NEXT.get(prov, {})
            sep = "&" if "?" in q["redirect_uri"] else "?"
            if user.get("deny"): return self.redirect(q["redirect_uri"] + sep + "error=access_denied&state=" + urllib.parse.quote(q["state"]))
            code = "bad-code" if user.get("badcode") else secrets.token_hex(8)
            CODES[code] = (prov, user, q["redirect_uri"], q["state"])
            return self.redirect(q["redirect_uri"] + sep + "code=" + code + "&state=" + urllib.parse.quote(q["state"]))
        if path in ("/kakao/v2/user/me", "/naver/v1/nid/me", "/google/userinfo"):
            tok = self.headers.get("Authorization", "").removeprefix("Bearer ")
            if tok not in TOKENS: return self.send(401, '{"msg":"invalid token"}')
            p, user = TOKENS.pop(tok)
            if p == "kakao":
                body = {"id": int(user["id"]), "properties": {"nickname": user["name"]},
                        "kakao_account": {"profile": {"nickname": user["name"]}, **({"email": user["email"]} if user.get("email") else {})}}
            elif p == "naver":
                body = {"resultcode": "00", "message": "success", "response": {"id": user["id"], "name": user["name"], "email": user.get("email", "")}}
            else:
                body = {"sub": user["id"], "name": user["name"], "email": user.get("email", ""), "email_verified": True}
            return self.send(200, json.dumps(body, ensure_ascii=False))
        self.send(404, "{}")

    def do_POST(self):
        n = int(self.headers.get("Content-Length", 0)); raw = self.rfile.read(n).decode()
        path = urllib.parse.urlparse(self.path).path
        if path == "/_next":
            d = json.loads(raw); NEXT[d["provider"]] = d; return self.send(200, "{}")
        if path in ("/kakao/oauth/token", "/naver/oauth2.0/token", "/google/token"):
            f = dict(urllib.parse.parse_qsl(raw)); prov = path.split("/")[1]
            cid, sec = CLIENT[prov]
            if f.get("grant_type") != "authorization_code" or f.get("client_id") != cid: return self.send(401, '{"error":"invalid_client"}')
            if sec and f.get("client_secret") != sec: return self.send(401, '{"error":"invalid_client_secret"}')
            c = CODES.pop(f.get("code"), None)
            if not c or c[0] != prov or f.get("code") == "bad-code": return self.send(400, '{"error":"invalid_grant"}')
            if f.get("redirect_uri") != c[2]: return self.send(400, '{"error":"redirect_uri_mismatch"}')
            if prov == "naver" and f.get("state") != c[3]: return self.send(400, '{"error":"invalid_request","error_description":"state"}')
            tok = secrets.token_hex(12); TOKENS[tok] = (prov, c[1])
            return self.send(200, json.dumps({"access_token": tok, "token_type": "bearer", "expires_in": 3600}))
        self.send(404, "{}")

if __name__ == "__main__":
    HTTPServer(("localhost", 8097), H).serve_forever()
