package com.portafolio;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@Controller
public class App {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ClaseRepository claseRepo;

    @Autowired
    private ArchivoRepository archivoRepo;

    private final Path rootLocation = Paths.get("uploads");

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner initData(UsuarioRepository uRepo) {
        return args -> {
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            // Sincronización estricta: Flor Xiomara Medina Salazar como Alumna Titular
            Usuario admin = uRepo.findByCodigo("ADMIN949163067").orElse(new Usuario("ADMIN949163067", "Flor Xiomara Medina Salazar", "s01269h@upla.edu.pe", 1));
            admin.setNombre("Flor Xiomara Medina Salazar");
            admin.setCorreo("s01269h@upla.edu.pe");
            admin.setRolId(1);
            uRepo.save(admin);

            if (uRepo.findByCodigo("EST2026").isEmpty()) {
                Usuario est = new Usuario("EST2026", "Visitante Académico / Auditor UPLA", "auditor@upla.edu.pe", 2);
                uRepo.save(est);
            }
        };
    }

    // ==========================================================
    // 1. PANTALLA DE ACCESO PRIVADO (ALTA SEGURIDAD)
    // ==========================================================

    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("usuario") != null) {
            return "redirect:/portafolio";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    @ResponseBody
    public String loginPage(@RequestParam(value = "error", required = false) String error) {
        return "<!DOCTYPE html>" +
        "<html lang='es'>" +
        "<head>" +
        "<meta charset='UTF-8'>" +
        "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>" +
        "<title>ACCESO PRIVADO // UPLA - Arquitectura de Software</title>" +
        "<link href='https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;600;700;800&family=Plus+Jakarta+Sans:wght@400;600;800;900&display=swap' rel='stylesheet'>" +
        "<style>" +
        "  * { box-sizing: border-box; margin: 0; padding: 0; }" +
        "  body { background: #05020c; font-family: 'Plus Jakarta Sans', sans-serif; color: #f5edff; min-height: 100vh; display: flex; justify-content: center; align-items: center; position: relative; overflow-x: hidden; padding: 20px; }" +
        "  canvas { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 1; pointer-events: none; }" +
        "  .terminal-container { position: relative; z-index: 10; width: 100%; max-width: 450px; }" +
        "  .card { background: rgba(18, 6, 36, 0.88); backdrop-filter: blur(25px); border: 1.5px solid rgba(216, 132, 255, 0.45); border-radius: 26px; padding: 42px 32px; box-shadow: 0 0 55px rgba(216, 132, 255, 0.35), 0 20px 60px rgba(0,0,0,0.85); animation: floatCard 6s ease-in-out infinite; }" +
        "  @keyframes floatCard { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-7px); } }" +
        "  .header-tag { display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px; font-family: 'Fira Code', monospace; font-size: 11px; color: #00f3ff; text-transform: uppercase; border-bottom: 1px dashed rgba(216, 132, 255, 0.25); padding-bottom: 9px; }" +
        "  .status-dot { display: inline-block; width: 9px; height: 9px; background: #00ff88; border-radius: 50%; box-shadow: 0 0 10px #00ff88; margin-right: 6px; animation: pulseDot 2s infinite; }" +
        "  @keyframes pulseDot { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.4; transform: scale(0.8); } }" +
        "  .logo-box { text-align: center; margin-bottom: 16px; }" +
        "  .logo-box img { height: 80px; filter: drop-shadow(0 0 20px rgba(216,132,255,0.85)); transition: 0.3s; }" +
        "  .title-box { text-align: center; margin-bottom: 26px; }" +
        "  .title-box h1 { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 22px; font-weight: 900; background: linear-gradient(135deg, #ffffff 0%, #ff80df 40%, #00f3ff 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-transform: uppercase; margin-bottom: 4px; filter: drop-shadow(0 0 14px rgba(216, 132, 255, 0.6)); }" +
        "  .title-box p { font-size: 11.5px; color: #d0b8ee; font-family: 'Fira Code', monospace; }" +
        "  .input-group { position: relative; margin-bottom: 20px; }" +
        "  .input-prefix { position: absolute; left: 16px; top: 50%; transform: translateY(-50%); font-family: 'Fira Code', monospace; font-size: 16px; color: #00f3ff; font-weight: bold; pointer-events: none; }" +
        "  input[type='password'], input[type='text'] { width: 100%; padding: 15px 15px 15px 48px; background: rgba(28, 10, 56, 0.95); border: 1.5px solid rgba(216, 132, 255, 0.35); border-radius: 14px; font-family: 'Fira Code', monospace; font-size: 15px; color: #00f3ff; outline: none; transition: 0.3s; letter-spacing: 3px; font-weight: 700; }" +
        "  input[type='password']:focus, input[type='text']:focus { border-color: #ff007f; box-shadow: 0 0 22px rgba(255, 0, 127, 0.5); }" +
        "  .btn-login { width: 100%; padding: 15px; background: linear-gradient(135deg, #d884ff 0%, #ff007f 100%); border: none; border-radius: 14px; font-family: 'Fira Code', monospace; font-size: 13.5px; font-weight: 800; color: #ffffff; letter-spacing: 1.5px; text-transform: uppercase; cursor: pointer; transition: 0.3s; box-shadow: 0 0 28px rgba(216, 132, 255, 0.5); margin-bottom: 20px; }" +
        "  .btn-login:hover { transform: translateY(-2px); box-shadow: 0 0 40px rgba(255, 0, 127, 0.8); filter: brightness(1.1); }" +
        "  .error-box { background: rgba(255, 0, 80, 0.18); border: 1px solid #ff0055; color: #ff6688; padding: 12px; border-radius: 10px; font-family: 'Fira Code', monospace; font-size: 11.5px; margin-bottom: 18px; text-align: center; }" +
        "  .lock-box { background: rgba(255, 255, 255, 0.03); border: 1px dashed rgba(216, 132, 255, 0.28); border-radius: 14px; padding: 14px; text-align: center; font-family: 'Fira Code', monospace; font-size: 11px; color: #d6c0f2; margin-bottom: 18px; }" +
        "  .telemetry { border-top: 1px dashed rgba(216, 132, 255, 0.25); padding-top: 14px; display: flex; justify-content: space-between; font-family: 'Fira Code', monospace; font-size: 10.5px; color: #bba2e2; }" +
        "</style>" +
        "</head>" +
        "<body>" +
        "<canvas id='canvas'></canvas>" +
        "<div class='terminal-container'>" +
        "  <div class='card'>" +
        "    <div class='header-tag'>" +
        "      <span><span class='status-dot'></span>UPLA // SYS_SECURE</span>" +
        "      <span>PORT: 8080</span>" +
        "    </div>" +
        "    <div class='logo-box'><img src='/image.png' alt='UPLA' onerror=\"this.src='https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Logo_Universidad_Peruana_Los_Andes.png/640px-Logo_Universidad_Peruana_Los_Andes.png'\"></div>" +
        "    <div class='title-box'>" +
        "      <h1>ARQUITECTURA DE SOFTWARE</h1>" +
        "      <p>Facultad de Ingeniería • EPISC 2026-I</p>" +
        "    </div>" +
        (error != null ? "    <div class='error-box'>⚠️ ACCESO DENEGADO // CLAVE NO REGISTRADA EN MYSQL</div>" : "") +
        "    <form action='/login' method='POST'>" +
        "      <div class='input-group'>" +
        "        <span class='input-prefix'>&gt;_</span>" +
        "        <input type='password' id='codInput' name='codigo' placeholder='••••••••••••' required autofocus autocomplete='off'>" +
        "      </div>" +
        "      <button type='submit' class='btn-login'>⚡ AUTENTICAR Y ACCEDER</button>" +
        "    </form>" +
        "    <div class='lock-box'>" +
        "      🔒 <b>PORTAFOLIO DE ACCESO PRIVADO</b><br>" +
        "      <span style='color:#a892cb;'>Autenticación segura vinculada a MySQL 8.0</span>" +
        "    </div>" +
        "    <div class='telemetry'>" +
        "      <span>TABLAS RELACIONALES ACTIVAS</span>" +
        "      <span style='color:#00ff88;'>● MYSQL 8.0 ONLINE</span>" +
        "    </div>" +
        "  </div>" +
        "</div>" +
        "<script>" +
        "  const canvas = document.getElementById('canvas'), ctx = canvas.getContext('2d');" +
        "  let w = canvas.width = window.innerWidth, h = canvas.height = window.innerHeight;" +
        "  window.onresize = () => { w = canvas.width = window.innerWidth; h = canvas.height = window.innerHeight; };" +
        "  const parts = [];" +
        "  for(let i=0; i<65; i++) parts.push({ x: Math.random()*w, y: Math.random()*h, vx: (Math.random()-0.5)*0.8, vy: (Math.random()-0.5)*0.8, r: 2.2 });" +
        "  const cometTrail = [];" +
        "  let mouse = { x: -1000, y: -1000 };" +
        "  window.addEventListener('mousemove', (e) => {" +
        "    const dx = e.clientX - mouse.x, dy = e.clientY - mouse.y; const speed = Math.hypot(dx, dy);" +
        "    mouse.x = e.clientX; mouse.y = e.clientY;" +
        "    for(let i=0; i<Math.min(Math.floor(speed/4)+2, 8); i++) {" +
        "      cometTrail.push({ x: mouse.x+(Math.random()-0.5)*6, y: mouse.y+(Math.random()-0.5)*6, vx: -dx*0.1+(Math.random()-0.5)*2, vy: -dy*0.1+(Math.random()-0.5)*2, r: Math.random()*3+1.2, alpha: 1, decay: Math.random()*0.03+0.02, color: Math.random()>0.5?'#00f3ff':'#d884ff' });" +
        "    }" +
        "  });" +
        "  function loop() {" +
        "    ctx.clearRect(0, 0, w, h);" +
        "    for(let i=0; i<parts.length; i++) {" +
        "      let p = parts[i]; p.x += p.vx; p.y += p.vy;" +
        "      if(p.x<0 || p.x>w) p.vx *= -1; if(p.y<0 || p.y>h) p.vy *= -1;" +
        "      ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, Math.PI*2); ctx.fillStyle = 'rgba(216,132,255,0.7)'; ctx.fill();" +
        "      for(let j=i+1; j<parts.length; j++) {" +
        "        let p2 = parts[j], d = Math.hypot(p.x-p2.x, p.y-p2.y);" +
        "        if(d < 120) {" +
        "          ctx.beginPath(); ctx.moveTo(p.x, p.y); ctx.lineTo(p2.x, p2.y);" +
        "          ctx.strokeStyle = `rgba(216,132,255,${0.35*(1-d/120)})`; ctx.lineWidth = 0.9; ctx.stroke();" +
        "        }" +
        "      }" +
        "    }" +
        "    for(let i=cometTrail.length-1; i>=0; i--) {" +
        "      const p = cometTrail[i]; p.x += p.vx; p.y += p.vy; p.alpha -= p.decay; p.r *= 0.96;" +
        "      if(p.alpha <= 0) { cometTrail.splice(i, 1); continue; }" +
        "      ctx.save(); ctx.shadowBlur=12; ctx.shadowColor=p.color; ctx.fillStyle=p.color; ctx.globalAlpha=p.alpha; ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, Math.PI*2); ctx.fill(); ctx.restore();" +
        "    }" +
        "    requestAnimationFrame(loop);" +
        "  }" +
        "  loop();" +
        "</script>" +
        "</body>" +
        "</html>";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam(value = "codigo", required = false) String codigo,
                                @RequestParam(value = "username", required = false) String username,
                                HttpSession session) {
        String clave = (codigo != null && !codigo.trim().isEmpty()) ? codigo : username;
        if (clave != null) {
            String cod = clave.trim().toUpperCase();
            Optional<Usuario> uOpt = usuarioRepo.findByCodigo(cod);
            if (uOpt.isPresent()) {
                Usuario u = uOpt.get();
                if (u.esAdmin()) {
                    u.setNombre("Flor Xiomara Medina Salazar");
                    u.setCorreo("s01269h@upla.edu.pe");
                    usuarioRepo.save(u);
                }
                session.setAttribute("usuario", u);
                session.setAttribute("justLoggedIn", true);
                return "redirect:/portafolio";
            }
        }
        return "redirect:/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam("nombre") String nombre,
                                   HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (u != null && u.esAdmin()) {
            u.setNombre(nombre.trim());
            usuarioRepo.save(u);
            session.setAttribute("usuario", u);
            return "redirect:/portafolio?tab=presentacion&msg=perfil_actualizado";
        }
        return "redirect:/portafolio?tab=presentacion";
    }

    // ==========================================================
    // 2. DASHBOARD CÓSMICO EXTRAVAGANTE (CON TOP HUD & DOCENTE MEJORADOS)
    // ==========================================================

    @GetMapping("/portafolio")
    @ResponseBody
    public String verPortafolio(@RequestParam(value = "tab", defaultValue = "presentacion") String activeTab,
                                @RequestParam(value = "msg", required = false) String msg,
                                HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "<script>window.location.href='/login';</script>";
        }

        boolean esAdmin = usuario.esAdmin();
        boolean justLoggedIn = Boolean.TRUE.equals(session.getAttribute("justLoggedIn"));
        if (justLoggedIn) {
            session.removeAttribute("justLoggedIn");
        }

        String nombreAlumna = "Flor Xiomara Medina Salazar";
        if (usuario.getNombre() != null && !usuario.getNombre().toLowerCase().contains("docente")) {
            nombreAlumna = usuario.getNombre();
        }

        List<Clase> todasLasClases = claseRepo.findAll();
        List<Archivo> todosLosArchivos = archivoRepo.findAll();

        String[] temasSemanas = {
            "Conceptos Fundamentales de la Arquitectura de Software y Ciclo de Vida",
            "Requerimientos Arquitectónicos y Atributos de Calidad (ISO/IEC 25010)",
            "Escenarios de Atributos de Calidad y Árbol de Utilidad (Utility Tree)",
            "Modelo de 4+1 Vistas de Philippe Kruchten y Diagramas de Despliegue",
            "Estilos Arquitectónicos: Capas, MVC, Hexagonal y Microkernel",
            "Arquitectura Orientada a Servicios (SOA) y APIs RESTful",
            "Arquitectura de Microservicios: Descomposición y Patrones de Integración",
            "Evaluación Parcial y Consolidación del Portafolio Arquitectónico Fase I",
            "Patrones de Diseño Arquitectónico Estructurales y Creacionales",
            "Tácticas de Arquitectura para Disponibilidad, Tolerancia a Fallos y Rendimiento",
            "Tácticas de Seguridad de Datos, Cifrado y Mantenibilidad",
            "Método de Evaluación de Arquitecturas de Software (ATAM)",
            "Arquitecturas Cloud Native, Contenedores Docker y Microservicios en la Nube",
            "Documentación Arquitectónica: Modelo C4 y Plantillas Estándar arc42",
            "Métricas de Calidad de Software, Refactorización y Deuda Técnica",
            "Sustentación Final del Portafolio de Arquitectura de Software 2026-I"
        };

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>");
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>");
        sb.append("<title>Portafolio • Arquitectura de Software | ").append(nombreAlumna).append("</title>");
        sb.append("<link href='https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;600;700;800;900&display=swap' rel='stylesheet'>");
        sb.append("<style>");
        sb.append("* { box-sizing:border-box; margin:0; padding:0; }");
        sb.append("body { background:#05020c; color:#f5edff; font-family:'Plus Jakarta Sans',sans-serif; min-height:100vh; position:relative; overflow-x:hidden; }");
        sb.append("canvas#bgCanvas { position:fixed; top:0; left:0; width:100%; height:100%; z-index:0; pointer-events:none; }");

        // Tipografías Neón
        sb.append(".neon-title { font-weight:900; background:linear-gradient(135deg, #ffffff 0%, #ff80df 30%, #d884ff 65%, #00f3ff 100%); -webkit-background-clip:text; -webkit-text-fill-color:transparent; filter:drop-shadow(0 0 15px rgba(216,132,255,0.75)); }");
        sb.append(".neon-sub { font-family:'Fira Code',monospace; color:#00f3ff; letter-spacing:1.5px; font-weight:700; text-shadow:0 0 10px rgba(0,243,255,0.6); }");

        // Luz de Meteorito en Movimiento Continuo (Barrido Láser)
        sb.append(".meteor-glow-title { background:linear-gradient(90deg, #fff 0%, #00f3ff 25%, #ff007f 50%, #d884ff 75%, #fff 100%); background-size:200% auto; -webkit-background-clip:text; -webkit-text-fill-color:transparent; animation:meteorSweep 4.5s linear infinite; font-weight:900; filter:drop-shadow(0 0 20px rgba(216,132,255,0.8)); }");
        sb.append("@keyframes meteorSweep { 0% { background-position:0% center; } 100% { background-position:200% center; } }");

        // Cyber Loader 1-100%
        sb.append("#cyberLoader { position:fixed; inset:0; background:#05020c; z-index:999999; display:flex; flex-direction:column; align-items:center; justify-content:center; transition:opacity 0.7s ease, visibility 0.7s ease; padding:20px; }");
        sb.append(".loader-card { background:rgba(20,7,40,0.94); border:1.5px solid rgba(216,132,255,0.5); border-radius:26px; padding:38px 42px; text-align:center; box-shadow:0 0 60px rgba(216,132,255,0.4); backdrop-filter:blur(25px); max-width:520px; width:100%; }");
        sb.append(".loader-pct { font-family:'Fira Code',monospace; font-size:52px; font-weight:900; color:#d884ff; text-shadow:0 0 35px rgba(216,132,255,0.9); margin:14px 0; }");
        sb.append(".loader-bar-bg { width:100%; height:9px; background:rgba(255,255,255,0.08); border-radius:20px; overflow:hidden; border:1px solid rgba(216,132,255,0.3); }");
        sb.append(".loader-bar-fill { height:100%; width:0%; background:linear-gradient(90deg, #00f3ff, #d884ff, #ff007f); border-radius:20px; box-shadow:0 0 20px #ff007f; transition:width 0.05s linear; }");
        sb.append(".loader-status { font-family:'Fira Code',monospace; font-size:11.5px; color:#00f3ff; letter-spacing:1px; margin-top:14px; font-weight:700; }");
        sb.append(".loader-welcome { display:none; font-size:19px; font-weight:900; color:#fff; text-shadow:0 0 22px #ff007f; margin-top:16px; }");

        // Top HUD Extraordinario (Barra superior de cristal galáctico)
        sb.append(".hud-top { background:rgba(12,4,26,0.95); backdrop-filter:blur(25px); border-bottom:2px solid transparent; border-image:linear-gradient(90deg, #ff007f, #d884ff, #00f3ff, #ff007f) 1; padding:16px 36px; display:flex; justify-content:space-between; align-items:center; position:sticky; top:0; z-index:1000; box-shadow:0 8px 45px rgba(0,0,0,0.9), 0 0 25px rgba(216,132,255,0.15); }");
        sb.append(".hud-left { display:flex; align-items:center; gap:16px; }");
        sb.append(".hud-logo { height:56px; filter:drop-shadow(0 0 18px rgba(0,243,255,0.9)); transition:0.3s; }");
        sb.append(".hud-logo:hover { transform:scale(1.08) rotate(3deg); filter:drop-shadow(0 0 25px #ff007f); }");
        sb.append(".hud-brand { font-size:17.5px; font-weight:900; color:#fff; letter-spacing:0.8px; text-shadow:0 0 15px rgba(0,243,255,0.5); }");
        sb.append(".hud-facultad { font-family:'Fira Code',monospace; font-size:11px; color:#d884ff; font-weight:700; letter-spacing:0.5px; }");
        
        sb.append(".telemetry-hud { display:flex; gap:12px; align-items:center; font-family:'Fira Code',monospace; font-size:11px; flex-wrap:wrap; }");
        sb.append(".hud-stat { background:rgba(28,10,56,0.92); border:1px solid rgba(216,132,255,0.35); padding:8px 15px; border-radius:12px; color:#d8c4f2; white-space:nowrap; box-shadow:inset 0 0 10px rgba(0,0,0,0.5); }");
        sb.append(".hud-stat b { color:#00f3ff; text-shadow:0 0 8px rgba(0,243,255,0.7); }");
        sb.append(".pulse-green { display:inline-block; width:8px; height:8px; background:#00ff88; border-radius:50%; box-shadow:0 0 10px #00ff88; margin-right:6px; animation:pulseDot 1.8s infinite; }");
        sb.append(".user-badge-admin { background:linear-gradient(135deg, rgba(255,0,127,0.22), rgba(216,132,255,0.22)); border:1.5px solid #ff007f; color:#ff80df; padding:8px 18px; border-radius:22px; font-weight:900; box-shadow:0 0 25px rgba(255,0,127,0.5); white-space:nowrap; font-size:12px; }");
        sb.append(".user-badge-est { background:rgba(0,243,255,0.18); border:1.5px solid #00f3ff; color:#00f3ff; padding:8px 18px; border-radius:22px; font-weight:900; white-space:nowrap; font-size:12px; }");
        sb.append(".btn-exit { background:rgba(255,0,80,0.18); border:1px solid #ff0055; color:#ff6688; padding:8px 16px; border-radius:10px; font-weight:800; text-decoration:none; transition:0.3s; white-space:nowrap; letter-spacing:0.5px; }");
        sb.append(".btn-exit:hover { background:#ff0055; color:#fff; box-shadow:0 0 22px #ff0055; transform:translateY(-1px); }");

        sb.append(".alert-top { background:rgba(0,243,255,0.14); border-bottom:1px solid #00f3ff; color:#00f3ff; padding:11px 20px; font-family:'Fira Code',monospace; font-size:12px; text-align:center; box-shadow:0 0 18px rgba(0,243,255,0.25); position:relative; z-index:900; }");

        // Barra de 3 Pestañas
        sb.append(".tabs-bar { display:flex; justify-content:center; gap:16px; margin:26px 0 20px; position:relative; z-index:10; flex-wrap:wrap; padding:0 15px; }");
        sb.append(".tab-link { padding:14px 28px; border-radius:16px; text-decoration:none; font-weight:800; font-size:12.5px; letter-spacing:1px; text-transform:uppercase; transition:0.3s; font-family:'Fira Code',monospace; display:inline-flex; align-items:center; gap:8px; }");
        sb.append(".tab-active { background:linear-gradient(135deg, #d884ff 0%, #ff007f 100%); color:#fff; box-shadow:0 0 32px rgba(216,132,255,0.75); transform:scale(1.02); }");
        sb.append(".tab-inactive { background:rgba(25,8,48,0.75); border:1px solid rgba(216,132,255,0.25); color:#c4a7f2; }");
        sb.append(".tab-inactive:hover { background:rgba(38,13,74,0.9); color:#fff; border-color:#d884ff; box-shadow:0 0 18px rgba(216,132,255,0.35); }");

        sb.append(".main-content { max-width:1320px; margin:0 auto; padding:15px 20px 95px; position:relative; z-index:10; }");

        // BANNER HERO CON ESCUDO FLOTANTE 3D
        sb.append(".hero-showcase { background:linear-gradient(135deg, rgba(20,7,42,0.92) 0%, rgba(10,3,24,0.95) 100%); border:1.8px solid rgba(0,243,255,0.45); border-radius:28px; padding:38px 42px; margin-bottom:32px; box-shadow:0 0 50px rgba(0,243,255,0.2), 0 20px 60px rgba(0,0,0,0.8); display:grid; grid-template-columns:1fr auto; gap:36px; align-items:center; position:relative; overflow:hidden; }");
        sb.append(".hero-showcase::before { content:''; position:absolute; top:-40%; right:-10%; width:320px; height:320px; background:radial-gradient(circle, rgba(255,0,127,0.22), transparent 70%); filter:blur(40px); pointer-events:none; }");
        sb.append(".hero-left-content { position:relative; z-index:2; }");
        sb.append(".hero-tags-row { display:flex; flex-wrap:wrap; gap:10px; margin-top:20px; }");
        sb.append(".hero-tag-item { background:rgba(255,255,255,0.04); border:1px solid rgba(216,132,255,0.28); border-radius:10px; padding:6px 14px; font-family:'Fira Code',monospace; font-size:11.5px; color:#cbd5e1; display:flex; align-items:center; gap:6px; }");
        sb.append(".hero-tag-item b { color:#00f3ff; }");
        
        // Escudo 3D Flotante UPLA
        sb.append(".upla-3d-box { position:relative; z-index:2; display:flex; flex-direction:column; align-items:center; justify-content:center; }");
        sb.append(".upla-3d-shield { width:140px; height:140px; border-radius:28px; background:linear-gradient(135deg, rgba(0,243,255,0.3), rgba(255,0,127,0.3)); padding:4px; box-shadow:0 0 45px rgba(0,243,255,0.45), 0 15px 35px rgba(0,0,0,0.8); animation:shieldFloat 4s ease-in-out infinite; }");
        sb.append("@keyframes shieldFloat { 0%, 100% { transform:translateY(0px) rotate(0deg); } 50% { transform:translateY(-8px) rotate(1.5deg); } }");
        sb.append(".upla-3d-inner { width:100%; height:100%; background:#090418; border-radius:24px; display:flex; align-items:center; justify-content:center; overflow:hidden; border:1px solid rgba(216,132,255,0.4); }");
        sb.append(".upla-3d-inner img { width:80%; height:80%; object-fit:contain; filter:drop-shadow(0 0 12px rgba(0,243,255,0.85)); }");
        sb.append(".upla-3d-caption { margin-top:10px; font-family:'Fira Code',monospace; font-size:11px; font-weight:800; color:#38bdf8; letter-spacing:1px; text-shadow:0 0 8px #00f3ff; }");

        // Tarjetas y Diseño General
        sb.append(".dossier-grid { display:grid; grid-template-columns:1fr 1fr; gap:26px; margin-bottom:32px; }");
        sb.append(".cyber-card { background:rgba(18,6,36,0.86); border:1.5px solid rgba(216,132,255,0.38); border-radius:26px; padding:32px; backdrop-filter:blur(25px); box-shadow:0 15px 45px rgba(0,0,0,0.65); position:relative; overflow:hidden; }");
        sb.append(".cyber-card::before { content:''; position:absolute; top:0; left:0; width:100%; height:4px; background:linear-gradient(90deg, #00f3ff, #d884ff, #ff007f); }");
        
        // Estilos de la Tarjeta del Docente Elevada
        sb.append(".docente-card-pro { background:linear-gradient(135deg, rgba(24,8,48,0.92) 0%, rgba(12,4,30,0.95) 100%); border-color:rgba(168,85,247,0.55); box-shadow:0 0 40px rgba(168,85,247,0.22), 0 15px 45px rgba(0,0,0,0.7); }");
        sb.append(".docente-header { display:flex; gap:20px; align-items:center; margin-bottom:22px; }");
        sb.append(".docente-avatar-frame { width:95px; height:95px; border-radius:24px; border:2.5px solid #a855f7; background:radial-gradient(circle, #2e0854 0%, #100220 100%); display:flex; align-items:center; justify-content:center; font-size:42px; box-shadow:0 0 35px rgba(168,85,247,0.6); position:relative; flex-shrink:0; }");
        sb.append(".docente-spec-grid { display:grid; grid-template-columns:repeat(2, 1fr); gap:14px; margin:20px 0; }");
        sb.append(".docente-spec-box { background:rgba(255,255,255,0.035); border:1px solid rgba(168,85,247,0.3); border-radius:14px; padding:13px 15px; }");
        sb.append(".btn-docente-mail { display:inline-flex; align-items:center; gap:8px; background:linear-gradient(135deg, rgba(168,85,247,0.25), rgba(0,243,255,0.25)); border:1.5px solid #a855f7; color:#fff; padding:10px 20px; border-radius:12px; font-family:'Fira Code',monospace; font-size:11.5px; font-weight:800; text-decoration:none; transition:0.3s; margin-top:6px; }");
        sb.append(".btn-docente-mail:hover { background:#a855f7; color:#fff; box-shadow:0 0 25px #a855f7; transform:translateY(-1px); }");

        // Alumna Header
        sb.append(".student-header { display:flex; gap:22px; align-items:center; margin-bottom:22px; }");
        sb.append(".avatar-frame { width:95px; height:95px; border-radius:24px; border:2.5px solid #ff007f; background:radial-gradient(circle, #381266 0%, #150529 100%); display:flex; align-items:center; justify-content:center; font-size:42px; box-shadow:0 0 35px rgba(255,0,127,0.6); position:relative; flex-shrink:0; }");
        sb.append(".chip-badge { position:absolute; bottom:-6px; right:-6px; background:#ff007f; color:#fff; font-size:9.5px; font-family:'Fira Code',monospace; padding:3px 9px; border-radius:12px; font-weight:900; box-shadow:0 0 10px #ff007f; }");
        sb.append(".student-name { font-size:24px; font-weight:900; color:#fff; text-shadow:0 0 18px rgba(216,132,255,0.6); margin-bottom:4px; }");
        sb.append(".student-title { font-family:'Fira Code',monospace; font-size:12px; color:#00f3ff; letter-spacing:1px; font-weight:700; }");

        sb.append(".meta-list { display:grid; grid-template-columns:repeat(2, 1fr); gap:14px; margin:20px 0; }");
        sb.append(".meta-item { background:rgba(255,255,255,0.035); border:1px solid rgba(216,132,255,0.22); border-radius:14px; padding:13px 15px; }");
        sb.append(".meta-lbl { font-size:10.5px; text-transform:uppercase; color:#c7a6f7; font-family:'Fira Code',monospace; margin-bottom:4px; font-weight:700; }");
        sb.append(".meta-val { font-size:13.5px; font-weight:800; color:#fff; }");

        sb.append(".edit-profile-btn { background:rgba(216,132,255,0.18); border:1px solid #d884ff; color:#fff; padding:10px 22px; border-radius:12px; font-size:12px; font-weight:800; cursor:pointer; font-family:'Fira Code',monospace; transition:0.3s; margin-top:10px; }");
        sb.append(".edit-profile-btn:hover { background:#d884ff; color:#070210; box-shadow:0 0 25px #d884ff; }");
        sb.append(".readonly-tag { font-family:'Fira Code',monospace; font-size:11px; color:#9d85c8; background:rgba(255,255,255,0.03); padding:8px 15px; border-radius:10px; border:1px dashed rgba(255,255,255,0.18); display:inline-block; margin-top:10px; }");

        // Reactores
        sb.append(".units-grid { display:grid; grid-template-columns:repeat(4, 1fr); gap:18px; margin-top:18px; }");
        sb.append(".unit-card { background:rgba(18,6,36,0.75); border:1px solid rgba(216,132,255,0.28); border-radius:20px; padding:22px; position:relative; transition:0.3s; }");
        sb.append(".unit-card:hover { border-color:#00f3ff; transform:translateY(-3px); box-shadow:0 10px 30px rgba(0,243,255,0.25); }");
        sb.append(".unit-badge { font-family:'Fira Code',monospace; font-size:10px; font-weight:900; color:#ff007f; background:rgba(255,0,127,0.18); padding:4px 10px; border-radius:8px; display:inline-block; margin-bottom:10px; }");
        sb.append(".unit-title { font-size:13px; font-weight:800; color:#fff; margin-bottom:14px; line-height:1.45; min-height:52px; }");
        sb.append(".reactor-bar { height:7px; background:rgba(255,255,255,0.08); border-radius:10px; overflow:hidden; margin-bottom:9px; }");
        sb.append(".reactor-fill { height:100%; border-radius:10px; }");

        // Dropzone & Consola
        sb.append(".upload-console { background:rgba(20,7,40,0.88); border:1.5px solid rgba(216,132,255,0.4); border-radius:26px; padding:30px; margin-bottom:32px; backdrop-filter:blur(25px); box-shadow:0 12px 45px rgba(0,0,0,0.55); }");
        sb.append(".console-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; border-bottom:1px solid rgba(216,132,255,0.25); padding-bottom:12px; }");
        sb.append(".console-title { font-size:16.5px; font-weight:900; color:#fff; }");

        sb.append(".form-grid { display:grid; grid-template-columns:1fr 1fr; gap:18px; }");
        sb.append(".cyber-select, .cyber-input, .cyber-textarea { width:100%; background:rgba(28,10,56,0.9); border:1px solid rgba(216,132,255,0.32); border-radius:12px; padding:13px 15px; color:#fff; font-size:13px; outline:none; font-family:'Plus Jakarta Sans',sans-serif; transition:0.3s; }");
        sb.append(".cyber-select:focus, .cyber-input:focus, .cyber-textarea:focus { border-color:#ff007f; box-shadow:0 0 18px rgba(255,0,127,0.45); }");

        sb.append(".type-selector { display:flex; gap:14px; margin:9px 0; }");
        sb.append(".radio-btn-label { flex:1; background:rgba(255,255,255,0.035); border:1px solid rgba(216,132,255,0.28); border-radius:14px; padding:12px; cursor:pointer; display:flex; align-items:center; gap:8px; font-size:12px; font-weight:800; transition:0.3s; }");
        sb.append(".radio-btn-label:hover { border-color:#d884ff; background:rgba(216,132,255,0.12); }");
        sb.append(".radio-btn-label input[type='radio'] { accent-color:#ff007f; width:17px; height:17px; }");

        sb.append(".cyber-dropzone { border:2px dashed rgba(216,132,255,0.5); background:rgba(28,10,56,0.65); border-radius:18px; padding:24px; text-align:center; cursor:pointer; transition:0.3s; margin-top:15px; }");
        sb.append(".cyber-dropzone:hover { border-color:#ff007f; background:rgba(255,0,127,0.1); box-shadow:0 0 30px rgba(255,0,127,0.35); transform:translateY(-2px); }");
        sb.append(".dropzone-icon { font-size:32px; margin-bottom:6px; filter:drop-shadow(0 0 12px #d884ff); }");
        sb.append(".dropzone-text { font-size:13px; font-weight:800; color:#fff; margin-bottom:3px; }");
        sb.append(".dropzone-sub { font-size:11.5px; font-family:'Fira Code',monospace; color:#00f3ff; }");

        sb.append(".submit-btn { background:linear-gradient(135deg, #d884ff 0%, #ff007f 100%); border:none; border-radius:14px; padding:15px 25px; color:#fff; font-weight:900; font-size:13px; letter-spacing:1px; cursor:pointer; transition:0.3s; box-shadow:0 0 24px rgba(216,132,255,0.45); text-transform:uppercase; font-family:'Fira Code',monospace; margin-top:18px; width:100%; }");
        sb.append(".submit-btn:hover { transform:scale(1.01); box-shadow:0 0 35px rgba(255,0,127,0.75); filter:brightness(1.1); }");

        // Carrusel
        sb.append(".carousel-nav { display:flex; justify-content:space-between; align-items:center; margin-bottom:22px; gap:10px; }");
        sb.append(".nav-arrow-btn { background:rgba(28,10,56,0.9); border:1.5px solid #d884ff; color:#fff; padding:11px 20px; border-radius:14px; font-weight:900; font-size:12.5px; font-family:'Fira Code',monospace; cursor:pointer; transition:0.3s; white-space:nowrap; }");
        sb.append(".nav-arrow-btn:hover { background:#d884ff; color:#070210; box-shadow:0 0 25px #d884ff; }");
        sb.append(".week-pill-scroller { display:flex; gap:9px; overflow-x:auto; padding:8px 0; margin-bottom:22px; scrollbar-width:thin; }");
        sb.append(".week-pill { background:rgba(20,7,40,0.75); border:1px solid rgba(216,132,255,0.28); color:#caaef5; padding:8px 16px; border-radius:22px; font-family:'Fira Code',monospace; font-size:11px; font-weight:800; cursor:pointer; white-space:nowrap; transition:0.3s; }");
        sb.append(".week-pill:hover, .week-pill.active-pill { background:linear-gradient(135deg, #d884ff, #ff007f); color:#fff; border-color:transparent; box-shadow:0 0 18px rgba(216,132,255,0.55); }");

        sb.append(".week-card { display:none; background:rgba(18,6,36,0.85); border:1.5px solid rgba(216,132,255,0.38); border-radius:24px; padding:30px; backdrop-filter:blur(25px); box-shadow:0 15px 50px rgba(0,0,0,0.7); position:relative; }");
        sb.append(".week-card.active-week { display:block; animation:fadeInWeek 0.4s ease forwards; }");
        sb.append("@keyframes fadeInWeek { from { opacity:0; transform:translateY(15px); } to { opacity:1; transform:translateY(0); } }");

        sb.append(".week-card-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:26px; border-bottom:1px solid rgba(216,132,255,0.22); padding-bottom:18px; gap:10px; }");
        sb.append(".week-badge-lg { font-family:'Fira Code',monospace; font-size:12.5px; font-weight:900; color:#00f3ff; background:rgba(0,243,255,0.14); border:1px solid #00f3ff; padding:6px 14px; border-radius:10px; }");
        sb.append(".week-card-title { font-size:20px; font-weight:900; color:#fff; margin-top:6px; line-height:1.3; }");

        sb.append(".week-dual-grid { display:grid; grid-template-columns:1fr 1fr; gap:22px; }");
        sb.append(".compartment { background:rgba(255,255,255,0.025); border:1px solid rgba(216,132,255,0.22); border-radius:18px; padding:22px; }");
        sb.append(".compartment-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:18px; padding-bottom:10px; border-bottom:1px solid rgba(255,255,255,0.07); }");
        sb.append(".comp-title { font-size:14px; font-weight:900; display:flex; align-items:gap:8px; }");
        sb.append(".comp-mat { color:#00f3ff; text-shadow:0 0 10px rgba(0,243,255,0.4); }");
        sb.append(".comp-tar { color:#ff007f; text-shadow:0 0 10px rgba(255,0,127,0.4); }");

        sb.append(".doc-card { background:rgba(30,11,60,0.6); border:1px solid rgba(216,132,255,0.28); border-radius:15px; padding:18px; margin-bottom:15px; transition:0.3s; }");
        sb.append(".doc-card:hover { border-color:#d884ff; transform:translateY(-2px); box-shadow:0 6px 24px rgba(216,132,255,0.25); }");
        sb.append(".doc-title { font-size:14px; font-weight:800; color:#fff; margin-bottom:6px; }");
        sb.append(".doc-desc { font-size:12.5px; color:#d2bfec; margin-bottom:14px; line-height:1.45; }");
        sb.append(".doc-actions { display:flex; gap:9px; align-items:center; flex-wrap:wrap; }");
        sb.append(".btn-view { background:rgba(0,243,255,0.16); border:1px solid #00f3ff; color:#00f3ff; padding:7px 14px; border-radius:9px; font-size:11px; font-weight:800; text-decoration:none; cursor:pointer; font-family:'Fira Code',monospace; transition:0.2s; }");
        sb.append(".btn-view:hover { background:#00f3ff; color:#070210; }");
        sb.append(".btn-down { background:rgba(216,132,255,0.16); border:1px solid #d884ff; color:#d884ff; padding:7px 14px; border-radius:9px; font-size:11px; font-weight:800; text-decoration:none; font-family:'Fira Code',monospace; transition:0.2s; }");
        sb.append(".btn-down:hover { background:#d884ff; color:#070210; }");
        sb.append(".btn-del { background:rgba(255,0,80,0.18); border:1px solid #ff0055; color:#ff6688; padding:7px 12px; border-radius:9px; font-size:11.5px; cursor:pointer; margin-left:auto; }");
        sb.append(".btn-del:hover { background:#ff0055; color:#fff; }");

        sb.append(".empty-slot { padding:28px; text-align:center; border:1.5px dashed rgba(216,132,255,0.2); border-radius:16px; color:#957eb5; font-family:'Fira Code',monospace; font-size:12px; }");

        // Carnet Estudiantil Digital y Enlaces Académicos
        sb.append(".contact-hub-grid { display:grid; grid-template-columns:1.2fr 1.3fr; gap:26px; margin-top:12px; }");
        sb.append(".student-id-card { background:linear-gradient(135deg, rgba(28,9,56,0.95) 0%, rgba(14,4,30,0.95) 100%); border:2px solid #ff007f; border-radius:24px; padding:28px; position:relative; overflow:hidden; box-shadow:0 0 50px rgba(255,0,127,0.35); }");
        sb.append(".student-id-card::before { content:''; position:absolute; inset:0; background:linear-gradient(125deg, transparent 30%, rgba(216,132,255,0.15) 45%, rgba(0,243,255,0.15) 55%, transparent 70%); pointer-events:none; animation:hologramScan 6s infinite linear; }");
        sb.append("@keyframes hologramScan { 0% { transform:translateX(-100%); } 100% { transform:translateX(100%); } }");
        
        sb.append(".id-header { display:flex; justify-content:space-between; align-items:center; border-bottom:1.5px solid rgba(216,132,255,0.3); padding-bottom:14px; margin-bottom:20px; }");
        sb.append(".id-chip { width:42px; height:32px; background:linear-gradient(135deg, #ffd700, #ff8800); border-radius:6px; box-shadow:0 0 15px rgba(255,215,0,0.6); position:relative; }");
        sb.append(".id-chip::after { content:''; position:absolute; top:8px; left:0; right:0; height:1px; background:#885500; }");
        
        sb.append(".id-body { display:flex; gap:20px; align-items:center; margin-bottom:22px; }");
        sb.append(".id-photo { width:95px; height:115px; border-radius:18px; border:2px solid #00f3ff; background:radial-gradient(circle, #311059, #070210); display:flex; align-items:center; justify-content:center; font-size:46px; box-shadow:0 0 25px rgba(0,243,255,0.4); flex-shrink:0; position:relative; }");
        sb.append(".id-photo-badge { position:absolute; bottom:4px; font-size:8.5px; background:#00f3ff; color:#000; font-family:'Fira Code',monospace; font-weight:900; padding:2px 6px; border-radius:8px; }");
        
        sb.append(".id-details { flex:1; }");
        sb.append(".id-name { font-size:20px; font-weight:900; color:#fff; text-shadow:0 0 12px rgba(216,132,255,0.5); margin-bottom:4px; line-height:1.2; }");
        sb.append(".id-spec { font-family:'Fira Code',monospace; font-size:11.5px; color:#00f3ff; margin-bottom:8px; font-weight:700; }");
        sb.append(".id-code { font-family:'Fira Code',monospace; font-size:11px; color:#d8bbf7; background:rgba(255,255,255,0.04); padding:4px 8px; border-radius:6px; display:inline-block; }");
        
        // Sello Criptográfico Digital UPLA (Sin falso QR ni código de barras)
        sb.append(".digital-cert-seal { background:rgba(0,243,255,0.08); border:1.5px solid #00f3ff; border-radius:14px; padding:14px; margin-top:16px; text-align:center; box-shadow:0 0 20px rgba(0,243,255,0.25); }");

        sb.append(".contact-links-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-top:16px; }");
        sb.append(".social-card { background:rgba(24,8,46,0.85); border:1.5px solid rgba(216,132,255,0.25); border-radius:18px; padding:20px; transition:0.3s; display:flex; flex-direction:column; justify-content:space-between; text-decoration:none; }");
        sb.append(".social-card:hover { border-color:#00f3ff; transform:translateY(-4px); box-shadow:0 12px 30px rgba(0,243,255,0.25); filter:brightness(1.1); }");
        sb.append(".social-head { display:flex; align-items:center; gap:12px; margin-bottom:12px; }");
        sb.append(".social-icon { font-size:28px; filter:drop-shadow(0 0 10px rgba(216,132,255,0.7)); }");
        sb.append(".social-title { font-size:14px; font-weight:900; color:#fff; }");
        sb.append(".social-tag { font-size:10px; font-family:'Fira Code',monospace; color:#00f3ff; text-transform:uppercase; }");
        sb.append(".social-desc { font-size:12px; color:#d0bbf2; line-height:1.4; margin-bottom:12px; }");
        sb.append(".social-action { font-family:'Fira Code',monospace; font-size:11px; font-weight:800; color:#ff007f; display:flex; align-items:center; gap:4px; }");

        // Modales
        sb.append(".cyber-modal { position:fixed; inset:0; background:rgba(5,2,12,0.88); backdrop-filter:blur(22px); z-index:99999; display:flex; align-items:center; justify-content:center; padding:15px; }");
        sb.append(".modal-dialog { background:rgba(18,6,36,0.96); border:1.5px solid #d884ff; border-radius:26px; padding:28px; width:100%; max-width:1050px; box-shadow:0 0 60px rgba(216,132,255,0.45); position:relative; }");
        sb.append(".modal-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; border-bottom:1px solid rgba(216,132,255,0.25); padding-bottom:12px; }");
        sb.append(".modal-title { font-size:16px; font-weight:900; color:#00f3ff; font-family:'Fira Code',monospace; }");
        sb.append(".modal-close { background:rgba(255,0,80,0.2); border:1px solid #ff0055; color:#ff6688; padding:7px 14px; border-radius:9px; cursor:pointer; font-weight:800; font-size:12.5px; }");

        // Responsivo Celular y Laptop
        sb.append("@media (max-width: 900px) {");
        sb.append("  .hud-top { flex-direction: column; gap: 12px; padding: 14px 16px; }");
        sb.append("  .hud-left { flex-direction: column; text-align: center; gap: 8px; }");
        sb.append("  .telemetry-hud { flex-wrap: wrap; justify-content: center; gap: 8px; width: 100%; }");
        sb.append("  .hud-stat, .user-badge-admin, .user-badge-est, .btn-exit { font-size: 10px; padding: 6px 10px; }");
        sb.append("  .hero-showcase { grid-template-columns: 1fr; text-align: center; padding: 24px 20px; }");
        sb.append("  .hero-tags-row { justify-content: center; }");
        sb.append("  .upla-3d-box { margin-top: 15px; }");
        sb.append("  .tabs-bar { flex-direction: column; padding: 0 10px; gap: 8px; }");
        sb.append("  .tab-link { justify-content: center; font-size: 11px; padding: 12px; }");
        sb.append("  .dossier-grid, .units-grid, .week-dual-grid, .contact-hub-grid, .form-grid, .contact-links-grid, .docente-spec-grid { grid-template-columns: 1fr !important; }");
        sb.append("  .student-header, .docente-header { flex-direction: column; text-align: center; }");
        sb.append("  .meta-list { grid-template-columns: 1fr; }");
        sb.append("  .type-selector { flex-direction: column; }");
        sb.append("  .week-card-header { flex-direction: column; align-items: flex-start; }");
        sb.append("  .carousel-nav { flex-direction: column; gap: 8px; }");
        sb.append("  .nav-arrow-btn { width: 100%; text-align: center; }");
        sb.append("  .main-content { padding: 15px 12px 100px; }");
        sb.append("  .cyber-card, .upload-console, .week-card, .student-id-card { padding: 22px 18px; border-radius: 20px; }");
        sb.append("  .id-body { flex-direction: column; text-align: center; }");
        sb.append("  #michiWindow { width: calc(100% - 30px) !important; right: 15px !important; bottom: 95px !important; }");
        sb.append("  #cyberCatContainer { bottom: 18px; right: 18px; }");
        sb.append("}");

        // ==========================================================
        // ANIMALITO COMPLETO: MICHI 🐱🐾
        // ==========================================================
        sb.append("#cyberCatContainer { position:fixed; bottom:25px; right:25px; z-index:99998; cursor:pointer; display:flex; flex-direction:column; align-items:center; transition:transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }");
        sb.append("#cyberCatContainer:hover { transform:scale(1.12) translateY(-6px); }");
        sb.append(".cat-bubble { background:rgba(18,6,36,0.94); border:1.5px solid #00f3ff; color:#00f3ff; font-family:'Fira Code',monospace; font-size:10.5px; font-weight:800; padding:5px 12px; border-radius:14px; margin-bottom:8px; box-shadow:0 0 16px rgba(0,243,255,0.45); animation:bubbleBounce 2s ease-in-out infinite alternate; pointer-events:none; white-space:nowrap; }");
        sb.append("@keyframes bubbleBounce { from { transform:translateY(0); } to { transform:translateY(-5px); } }");
        
        sb.append(".cat-sprite { width:90px; height:80px; filter:drop-shadow(0 0 15px rgba(255,0,127,0.7)) drop-shadow(0 0 25px rgba(216,132,255,0.4)); animation:catHover 2.5s ease-in-out infinite alternate; }");
        sb.append("@keyframes catHover { 0% { transform:translateY(0px) rotate(-2deg); } 50% { transform:translateY(-8px) rotate(2deg); } 100% { transform:translateY(0px) rotate(-2deg); } }");
        
        sb.append(".cat-tail { transform-origin: 30px 60px; animation: tailWag 1.8s ease-in-out infinite alternate; }");
        sb.append("@keyframes tailWag { 0% { transform: rotate(-15deg); } 100% { transform: rotate(20deg); } }");
        
        sb.append(".cat-ear-left { transform-origin: 35px 25px; animation: earTwitch 3.5s ease-in-out infinite; }");
        sb.append(".cat-ear-right { transform-origin: 65px 25px; animation: earTwitch 3.5s ease-in-out infinite 0.3s; }");
        sb.append("@keyframes earTwitch { 0%, 90%, 100% { transform: rotate(0deg); } 93% { transform: rotate(-10deg); } 96% { transform: rotate(8deg); } }");
        
        sb.append(".cat-paw-front { animation: pawStep 1.2s ease-in-out infinite alternate; }");
        sb.append(".cat-paw-back { animation: pawStep 1.2s ease-in-out infinite alternate-reverse; }");
        sb.append("@keyframes pawStep { 0% { transform: translateY(0); } 100% { transform: translateY(-3px); } }");
        
        sb.append(".cat-eye { animation: catBlink 4s infinite; transform-origin: center; }");
        sb.append("@keyframes catBlink { 0%, 95%, 100% { transform: scaleY(1); } 97% { transform: scaleY(0.1); } }");

        // Ventana de Chat de MICHI
        sb.append("#michiWindow { position:fixed; bottom:95px; right:25px; width:410px; max-height:580px; height:80vh; background:rgba(18,6,36,0.96); border:1.5px solid #d884ff; border-radius:24px; box-shadow:0 15px 60px rgba(0,0,0,0.85), 0 0 40px rgba(216,132,255,0.35); backdrop-filter:blur(25px); z-index:99999; display:none; flex-direction:column; overflow:hidden; }");
        sb.append(".michi-head { background:rgba(30,10,60,0.85); border-bottom:1px solid rgba(216,132,255,0.25); padding:14px 18px; display:flex; justify-content:space-between; align-items:center; }");
        sb.append(".michi-title { font-size:13.5px; font-weight:900; color:#00f3ff; font-family:'Fira Code',monospace; display:flex; align-items:center; gap:8px; }");
        sb.append(".michi-body { flex:1; padding:15px; overflow-y:auto; display:flex; flex-direction:column; gap:12px; scrollbar-width:thin; }");
        sb.append(".michi-msg { max-width:88%; padding:11px 15px; border-radius:14px; font-size:12.5px; line-height:1.5; font-family:'Plus Jakarta Sans',sans-serif; }");
        sb.append(".msg-bot { background:rgba(216,132,255,0.14); border:1px solid rgba(216,132,255,0.32); color:#f5edff; align-self:flex-start; border-bottom-left-radius:3px; }");
        sb.append(".msg-user { background:linear-gradient(135deg, #d884ff, #ff007f); color:#fff; align-self:flex-end; border-bottom-right-radius:3px; font-weight:700; }");
        sb.append(".michi-chips { display:flex; gap:6px; overflow-x:auto; padding:8px 12px; border-top:1px solid rgba(255,255,255,0.06); scrollbar-width:none; }");
        sb.append(".m-chip { background:rgba(255,255,255,0.04); border:1px solid rgba(216,132,255,0.25); padding:5px 10px; border-radius:12px; font-size:10px; font-family:'Fira Code',monospace; color:#d884ff; cursor:pointer; white-space:nowrap; transition:0.2s; }");
        sb.append(".m-chip:hover { border-color:#00f3ff; color:#00f3ff; background:rgba(0,243,255,0.1); }");
        sb.append(".michi-input-box { padding:10px 12px; border-top:1px solid rgba(216,132,255,0.22); display:flex; gap:8px; background:rgba(10,3,20,0.6); }");
        sb.append(".michi-input { flex:1; background:rgba(28,10,56,0.85); border:1px solid rgba(216,132,255,0.3); border-radius:10px; padding:10px 12px; color:#fff; font-size:12px; outline:none; font-family:'Plus Jakarta Sans',sans-serif; }");
        sb.append(".michi-send-btn { background:#ff007f; border:none; border-radius:10px; color:#fff; padding:0 14px; font-size:12px; cursor:pointer; font-weight:800; font-family:'Fira Code',monospace; transition:0.2s; }");
        sb.append(".michi-send-btn:hover { background:#00f3ff; color:#070210; }");

        sb.append("</style></head><body>");

        // Lienzo Cósmico con Meteoritos y Estrellas Fugaces
        sb.append("<canvas id='bgCanvas'></canvas>");

        // Loader 1-100%
        if (justLoggedIn) {
            sb.append("<div id='cyberLoader'>");
            sb.append("  <div class='loader-card'>");
            sb.append("    <img src='/image.png' alt='UPLA' style='height:75px; filter:drop-shadow(0 0 20px #d884ff); margin-bottom:10px;' onerror=\"this.src='https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Logo_Universidad_Peruana_Los_Andes.png/640px-Logo_Universidad_Peruana_Los_Andes.png'\">");
            sb.append("    <div style='font-family:\"Fira Code\",monospace; font-size:11.5px; color:#d884ff; letter-spacing:2px; font-weight:800;'>SISTEMA ARQUITECTÓNICO UPLA 2026-I</div>");
            sb.append("    <div class='loader-pct' id='loaderPct'>000%</div>");
            sb.append("    <div class='loader-bar-bg'><div class='loader-bar-fill' id='loaderFill'></div></div>");
            sb.append("    <div class='loader-status' id='loaderStatus'>SINCRONIZANDO KERNEL CIBERNÉTICO...</div>");
            if (esAdmin) {
                sb.append("    <div class='loader-welcome' id='loaderWelcome'>¡BIENVENIDA, ALUMNA TITULAR ").append(nombreAlumna.toUpperCase()).append("!</div>");
            } else {
                sb.append("    <div class='loader-welcome' id='loaderWelcome'>¡BIENVENIDO, VISITANTE ACADÉMICO / AUDITOR!</div>");
            }
            sb.append("  </div>");
            sb.append("</div>");
        }

        // =======================================================
        // 1. TOP HUD EXTRAORDINARIO Y EXTRAVAGANTE
        // =======================================================
        sb.append("<header class='hud-top'>");
        sb.append("  <div class='hud-left'>");
        sb.append("    <img src='/image.png' alt='Logo UPLA' class='hud-logo' onerror=\"this.src='https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Logo_Universidad_Peruana_Los_Andes.png/640px-Logo_Universidad_Peruana_Los_Andes.png'\">");
        sb.append("    <div>");
        sb.append("      <div class='hud-brand'>UNIVERSIDAD PERUANA LOS ANDES</div>");
        sb.append("      <div class='hud-facultad'>FACULTAD DE INGENIERÍA // EPISC • ARQUITECTURA DE SOFTWARE 2026-I</div>");
        sb.append("    </div>");
        sb.append("  </div>");
        sb.append("  <div class='telemetry-hud'>");
        sb.append("    <div class='hud-stat'><span class='pulse-green'></span> MYSQL 8.0: <b>").append(todasLasClases.size()).append(" CLASES PERSISTIDAS</b></div>");
        sb.append("    <div class='hud-stat'>DOCUMENTOS: <b>").append(todosLosArchivos.size()).append(" EN BD</b></div>");
        sb.append("    <div class='hud-stat'>PUERTO: <b>8080</b></div>");
        if (esAdmin) {
            sb.append("    <span class='user-badge-admin'>🌸 ALUMNA TITULAR: ").append(nombreAlumna).append("</span>");
        } else {
            sb.append("    <span class='user-badge-est'>👁️ MODO AUDITOR: Visitante UPLA</span>");
        }
        sb.append("    <a href='/logout' class='btn-exit'>[ CERRAR SESIÓN ]</a>");
        sb.append("  </div>");
        sb.append("</header>");

        // Alertas
        if ("upload_ok".equals(msg)) {
            sb.append("<div class='alert-top'>✓ TRANSACCIÓN MYSQL EXITOSA: Documento clasificado y vinculado correctamente en la base de datos.</div>");
        } else if ("del_ok".equals(msg)) {
            sb.append("<div class='alert-top' style='border-color:#ff0055; color:#ff6688; background:rgba(255,0,80,0.15);'>✓ REGISTRO ELIMINADO: Fila retirada de MySQL y archivo desvinculado del servidor.</div>");
        } else if ("perfil_actualizado".equals(msg)) {
            sb.append("<div class='alert-top'>✓ PERFIL ACTUALIZADO: Nombre sincronizado en la base de datos MySQL.</div>");
        } else if ("msg_enviado".equals(msg)) {
            sb.append("<div class='alert-top' style='border-color:#00ff88; color:#00ff88; background:rgba(0,255,136,0.12);'>✓ MENSAJE TRANSMITIDO: Su consulta ha sido enviada al correo de Flor Xiomara.</div>");
        }

        // Pestañas Principales
        sb.append("<div class='tabs-bar'>");
        sb.append("  <a href='/portafolio?tab=presentacion' class='tab-link ").append("presentacion".equals(activeTab) ? "tab-active" : "tab-inactive").append("'>🌸 01. Presentación de la Estudiante</a>");
        sb.append("  <a href='/portafolio?tab=archivos' class='tab-link ").append("archivos".equals(activeTab) ? "tab-active" : "tab-inactive").append("'>📂 02. Portafolio Semanal & Tareas</a>");
        sb.append("  <a href='/portafolio?tab=contacto' class='tab-link ").append("contacto".equals(activeTab) ? "tab-active" : "tab-inactive").append("'>⚡ 03. Contacto & Redes</a>");
        sb.append("</div>");

        sb.append("<main class='main-content'>");

        // =======================================================
        // TAB 01: PRESENTACIÓN DE LA ESTUDIANTE & CÁTEDRA
        // =======================================================
        if ("presentacion".equals(activeTab)) {

            // 2. HERO SHOWCASE EXTRAVAGANTE CON ESCUDO FLOTANTE 3D UPLA
            sb.append("<section class='hero-showcase'>");
            sb.append("  <div class='hero-left-content'>");
            sb.append("    <span class='neon-sub'>// PORTAFOLIO ACADÉMICO DIGITAL OFICIAL</span>");
            sb.append("    <h1 class='meteor-glow-title' style='font-size:32px; margin:10px 0 8px;'>ARQUITECTURA DE SOFTWARE 2026-I</h1>");
            sb.append("    <p style='color:#cbd5e1; font-size:13.5px; line-height:1.65; max-width:820px;'>Evidencias de aprendizaje, requerimientos de calidad (ISO/IEC 25010), modelos de 4+1 vistas, diseño por capas, APIs RESTful en Spring Boot y persistencia relacional en MySQL 8.0.</p>");
            sb.append("    <div class='hero-tags-row'>");
            sb.append("      <div class='hero-tag-item'>🏛️ Código: <b>332181</b></div>");
            sb.append("      <div class='hero-tag-item'>📋 Plan: <b>2022</b></div>");
            sb.append("      <div class='hero-tag-item'>⭐ Créditos: <b>02</b></div>");
            sb.append("      <div class='hero-tag-item'>⏱️ Horas: <b>04 Prácticas</b></div>");
            sb.append("      <div class='hero-tag-item'>📍 Modalidad: <b>Presencial</b></div>");
            sb.append("      <div class='hero-tag-item'>🎓 Facultad: <b>Ingeniería // EPISC</b></div>");
            sb.append("    </div>");
            sb.append("  </div>");

            // Escudo 3D Flotante UPLA
            sb.append("  <div class='upla-3d-box'>");
            sb.append("    <div class='upla-3d-shield'>");
            sb.append("      <div class='upla-3d-inner'>");
            sb.append("        <img src='/image.png' alt='UPLA' onerror=\"this.src='https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Logo_Universidad_Peruana_Los_Andes.png/640px-Logo_Universidad_Peruana_Los_Andes.png'\">");
            sb.append("      </div>");
            sb.append("    </div>");
            sb.append("    <div class='upla-3d-caption'>UPLA • HUANCAYO</div>");
            sb.append("  </div>");
            sb.append("</section>");

            sb.append("<div class='dossier-grid'>");

            // Credencial de la Alumna Titular (Flor Xiomara Medina Salazar)
            sb.append("  <div class='cyber-card'>");
            sb.append("    <div class='student-header'>");
            sb.append("      <div class='avatar-frame'>🌸<div class='chip-badge'>AUTORA</div></div>");
            sb.append("      <div>");
            sb.append("        <div class='student-title'>// ALUMNA TITULAR • INGENIERÍA DE SISTEMAS</div>");
            sb.append("        <h2 class='student-name'>").append(nombreAlumna).append("</h2>");
            sb.append("        <div style='font-family:\"Fira Code\",monospace; font-size:12px; color:#cba6f7;'>UNIVERSIDAD PERUANA LOS ANDES • HUANCAYO</div>");
            sb.append("      </div>");
            sb.append("    </div>");

            sb.append("    <div class='meta-list'>");
            sb.append("      <div class='meta-item'><div class='meta-lbl'>Carrera Profesional</div><div class='meta-val'>Ingeniería de Sistemas y Computación</div></div>");
            sb.append("      <div class='meta-item'><div class='meta-lbl'>Correo Institucional</div><div class='meta-val' style='color:#00f3ff;'>s01269h@upla.edu.pe</div></div>");
            sb.append("      <div class='meta-item'><div class='meta-lbl'>Semestre Académico</div><div class='meta-val'>2026-I • Plan de Estudios 2022</div></div>");
            sb.append("      <div class='meta-item'><div class='meta-lbl'>Rol en el Sistema</div><div class='meta-val' style='color:#ff007f;'>Alumna Titular / Autora</div></div>");
            sb.append("    </div>");

            if (esAdmin) {
                sb.append("    <button class='edit-profile-btn' onclick=\"document.getElementById('editModal').style.display='flex';\">✏️ Editar Mi Nombre</button>");
            } else {
                sb.append("    <div class='readonly-tag'>🔒 Modo de solo lectura (Auditoría Académica UPLA)</div>");
            }
            sb.append("  </div>");

            // 3. FICHA DE CÁTEDRA DEL DOCENTE ELEVADA Y DE ALTO PRESTIGIO
            sb.append("  <div class='cyber-card docente-card-pro'>");
            sb.append("    <div class='docente-header'>");
            sb.append("      <div class='docente-avatar-frame'>👨‍🏫<div class='chip-badge' style='background:#a855f7; box-shadow:0 0 10px #a855f7;'>CÁTEDRA</div></div>");
            sb.append("      <div>");
            sb.append("        <div style='font-family:\"Fira Code\",monospace; font-size:11px; color:#c084fc; font-weight:800; letter-spacing:1px;'>// DIRECCIÓN DOCENTE • CÁTEDRA DE ARQUITECTURA</div>");
            sb.append("        <h2 style='font-size:23px; font-weight:900; color:#fff; text-shadow:0 0 15px rgba(168,85,247,0.7); margin:3px 0;'>Mg. Raúl Enrique Fernández Bejarano</h2>");
            sb.append("        <div style='font-family:\"Fira Code\",monospace; font-size:11.5px; color:#00f3ff;'>DOCENTE TITULAR DE LA ASIGNATURA</div>");
            sb.append("      </div>");
            sb.append("    </div>");

            sb.append("    <div class='docente-spec-grid'>");
            sb.append("      <div class='docente-spec-box'>");
            sb.append("        <div class='meta-lbl' style='color:#c084fc;'>Correo Oficial Cátedra</div>");
            sb.append("        <div class='meta-val' style='color:#00f3ff; font-size:12.5px; word-break:break-all;'>d.rfernandezb@ms.upla.edu.pe</div>");
            sb.append("      </div>");
            sb.append("      <div class='docente-spec-box'>");
            sb.append("        <div class='meta-lbl' style='color:#c084fc;'>Código & Créditos</div>");
            sb.append("        <div class='meta-val'>332181 • 02 Créditos</div>");
            sb.append("      </div>");
            sb.append("      <div class='docente-spec-box'>");
            sb.append("        <div class='meta-lbl' style='color:#c084fc;'>Carga Horaria Semanal</div>");
            sb.append("        <div class='meta-val'>04 Horas Prácticas</div>");
            sb.append("      </div>");
            sb.append("      <div class='docente-spec-box'>");
            sb.append("        <div class='meta-lbl' style='color:#c084fc;'>Semestre Académico</div>");
            sb.append("        <div class='meta-val'>2026-I (06 Abr - 26 Jul 2026)</div>");
            sb.append("      </div>");
            sb.append("      <div class='docente-spec-box'>");
            sb.append("        <div class='meta-lbl' style='color:#c084fc;'>Modalidad & Facultad</div>");
            sb.append("        <div class='meta-val'>Presencial • Pabellón EPISC</div>");
            sb.append("      </div>");
            sb.append("      <div class='docente-spec-box'>");
            sb.append("        <div class='meta-lbl' style='color:#c084fc;'>Especialidad Docente</div>");
            sb.append("        <div class='meta-val'>Arquitectura Cloud & Microservicios</div>");
            sb.append("      </div>");
            sb.append("    </div>");

            sb.append("    <a href='mailto:d.rfernandezb@ms.upla.edu.pe' class='btn-docente-mail'>✉️ Enviar Consulta al Mg. Raúl Fernández</a>");
            sb.append("  </div>");

            sb.append("</div>");

            // Sumilla Oficial y Logro de Aprendizaje del Sílabo
            sb.append("<div class='cyber-card' style='margin-bottom:26px;'>");
            sb.append("  <span class='neon-sub'>// SÍLABO OFICIAL UPLA • PLAN 2022</span>");
            sb.append("  <h2 class='neon-title' style='font-size:22px; margin:6px 0 16px;'>Sumilla y Competencia General</h2>");
            sb.append("  <div style='display:grid; grid-template-columns:1fr 1fr; gap:20px;'>");
            sb.append("    <div style='background:rgba(255,255,255,0.025); border:1px solid rgba(216,132,255,0.2); border-radius:16px; padding:20px;'>");
            sb.append("      <h4 style='color:#ff007f; font-family:\"Fira Code\",monospace; font-size:12px; margin-bottom:8px;'>📖 SUMILLA OFICIAL DE LA ASIGNATURA</h4>");
            sb.append("      <p style='font-size:12.5px; line-height:1.65; color:#d2bdef;'>Asignatura de naturaleza práctica orientada a comprender y formular soluciones de arquitectura de software bajo estándares internacionales (IEEE 1471, ISO/IEC 25010). Capacita en diseño por componentes, desacoplamiento, comunicación mediante APIs empresariales y adopción de frameworks modernos.</p>");
            sb.append("    </div>");
            sb.append("    <div style='background:rgba(255,255,255,0.025); border:1px solid rgba(216,132,255,0.2); border-radius:16px; padding:20px;'>");
            sb.append("      <h4 style='color:#00f3ff; font-family:\"Fira Code\",monospace; font-size:12px; margin-bottom:8px;'>🎯 COMPETENCIA & LOGRO GENERAL</h4>");
            sb.append("      <p style='font-size:12.5px; line-height:1.65; color:#d2bdef;'>Diseña, evalúa e implementa arquitecturas de software robustas, escalables y seguras utilizando POO, modelos 4+1 vistas de Kruchten, persistencia relacional en MySQL 8.0 y despliegues empresariales para resolver problemas tecnológicos contextualizados.</p>");
            sb.append("    </div>");
            sb.append("  </div>");
            sb.append("</div>");

            // Las 4 Unidades Oficiales del Sílabo
            sb.append("<div class='cyber-card'>");
            sb.append("  <span class='neon-sub'>// PROGRAMACIÓN DE CAPACIDADES</span>");
            sb.append("  <h2 class='neon-title' style='font-size:22px; margin:6px 0 16px;'>Reactores de Avance Curricular (4 Unidades)</h2>");
            sb.append("  <div class='units-grid'>");

            sb.append("    <div class='unit-card'>");
            sb.append("      <div class='unit-badge'>UNIDAD I • SEM 1-4</div>");
            sb.append("      <div class='unit-title'>Fundamentos y Estándares de Arquitectura de Software</div>");
            sb.append("      <div class='reactor-bar'><div class='reactor-fill' style='width:100%; background:#00ff88; box-shadow:0 0 10px #00ff88;'></div></div>");
            sb.append("      <div style='font-family:\"Fira Code\",monospace; font-size:11px; color:#00ff88; font-weight:800;'>AVANCE: 100%</div>");
            sb.append("    </div>");

            sb.append("    <div class='unit-card'>");
            sb.append("      <div class='unit-badge'>UNIDAD II • SEM 5-8</div>");
            sb.append("      <div class='unit-title'>Modelado de Arquitecturas con POO y Vistas 4+1</div>");
            sb.append("      <div class='reactor-bar'><div class='reactor-fill' style='width:100%; background:#00f3ff; box-shadow:0 0 10px #00f3ff;'></div></div>");
            sb.append("      <div style='font-family:\"Fira Code\",monospace; font-size:11px; color:#00f3ff; font-weight:800;'>AVANCE: 100%</div>");
            sb.append("    </div>");

            sb.append("    <div class='unit-card'>");
            sb.append("      <div class='unit-badge'>UNIDAD III • SEM 9-12</div>");
            sb.append("      <div class='unit-title'>Comunicación, Integración y Servicios Web REST</div>");
            sb.append("      <div class='reactor-bar'><div class='reactor-fill' style='width:75%; background:#d884ff; box-shadow:0 0 10px #d884ff;'></div></div>");
            sb.append("      <div style='font-family:\"Fira Code\",monospace; font-size:11px; color:#d884ff; font-weight:800;'>AVANCE: 75%</div>");
            sb.append("    </div>");

            sb.append("    <div class='unit-card'>");
            sb.append("      <div class='unit-badge'>UNIDAD IV • SEM 13-16</div>");
            sb.append("      <div class='unit-title'>Frameworks Modernos y Despliegue en Cloud Azure</div>");
            sb.append("      <div class='reactor-bar'><div class='reactor-fill' style='width:50%; background:#ff007f; box-shadow:0 0 10px #ff007f;'></div></div>");
            sb.append("      <div style='font-family:\"Fira Code\",monospace; font-size:11px; color:#ff007f; font-weight:800;'>AVANCE: 50%</div>");
            sb.append("    </div>");

            sb.append("  </div>");
            sb.append("</div>");
        }

        // =======================================================
        // TAB 02: PORTAFOLIO SEMANAL & TAREAS (CARRUSEL 1-16 DINÁMICO)
        // =======================================================
        if ("archivos".equals(activeTab)) {

            if (esAdmin) {
                sb.append("<div class='upload-console'>");
                sb.append("  <div class='console-header'>");
                sb.append("    <div class='console-title'>➕ PUBLICAR MATERIAL O TAREA EN MYSQL</div>");
                sb.append("    <span class='neon-sub'>// TRANSMISIÓN EN VIVO A BD MYSQL</span>");
                sb.append("  </div>");
                sb.append("  <form action='/clases/crear' method='POST' enctype='multipart/form-data'>");
                sb.append("    <div class='form-grid'>");
                sb.append("      <div>");
                // AQUÍ: Campo de texto libre para que Flor escriba cualquier semana o texto sin restricciones fijas
                sb.append("        <label style='font-size:11px; font-family:\"Fira Code\",monospace; color:#d884ff; display:block; margin-bottom:5px; font-weight:700;'>SEMANA ACADÉMICA (ESCRIBE EL N° O TEXTO):</label>");
                sb.append("        <input type='text' name='semana' id='formSemanaInput' class='cyber-input' placeholder='Escribe la semana (Ej: 1, 2, 3 o Semana 1)' value='1' required>");
                sb.append("      </div>");
                sb.append("      <div>");
                sb.append("        <label style='font-size:11px; font-family:\"Fira Code\",monospace; color:#d884ff; display:block; margin-bottom:5px; font-weight:700;'>TÍTULO DEL TEMA / TAREA SEGÚN EL DOCENTE:</label>");
                sb.append("        <input type='text' name='titulo' class='cyber-input' placeholder='Escribe el tema o título que dictó el Ingeniero...' required>");
                sb.append("      </div>");
                sb.append("    </div>");

                sb.append("    <div style='margin-top:14px;'>");
                sb.append("      <label style='font-size:11px; font-family:\"Fira Code\",monospace; color:#d884ff; display:block; margin-bottom:5px; font-weight:700;'>CLASIFICACIÓN DEL ARCHIVO (COMPARTIMENTO):</label>");
                sb.append("      <div class='type-selector'>");
                sb.append("        <label class='radio-btn-label'><input type='radio' name='tipo' value='MATERIAL' checked> 📚 Material de Clase (Teoría / Diapositivas)</label>");
                sb.append("        <label class='radio-btn-label'><input type='radio' name='tipo' value='TAREA'> 📝 Tarea Desarrollada (Práctica / Informe)</label>");
                sb.append("      </div>");
                sb.append("    </div>");

                sb.append("    <div style='margin-top:14px;'>");
                sb.append("      <label style='font-size:11px; font-family:\"Fira Code\",monospace; color:#d884ff; display:block; margin-bottom:5px; font-weight:700;'>DESCRIPCIÓN DEL CONTENIDO:</label>");
                sb.append("      <textarea name='descripcion' rows='2' class='cyber-textarea' placeholder='Resumen del material o solución de la tarea...' required></textarea>");
                sb.append("    </div>");

                sb.append("    <div class='cyber-dropzone' onclick=\"document.getElementById('fileInput').click();\">");
                sb.append("      <div class='dropzone-icon'>📤</div>");
                sb.append("      <div class='dropzone-text' id='dropzoneText'>Haz clic aquí para seleccionar o arrastrar tu documento</div>");
                sb.append("      <div class='dropzone-sub'>Formatos aceptados: PDF, Word (.docx), PowerPoint (.pptx), ZIP, Imágenes</div>");
                sb.append("      <input type='file' id='fileInput' name='archivo' style='display:none;' onchange=\"document.getElementById('dropzoneText').innerText = '✓ Archivo preparado: ' + this.files[0].name;\" required>");
                sb.append("    </div>");

                sb.append("    <button type='submit' class='submit-btn'>💾 GUARDAR Y PERSISTIR EN MYSQL (TABLA CLASES)</button>");
                sb.append("  </form>");
                sb.append("</div>");
            }

            // Controles del Carrusel 1-16
            sb.append("<div class='carousel-nav'>");
            sb.append("  <button class='nav-arrow-btn' onclick='changeWeek(-1)'>◀ SEMANA ANTERIOR</button>");
            sb.append("  <span class='neon-sub' id='carouselStatus'>NAVEGANDO SEMANA 01 DE 16</span>");
            sb.append("  <button class='nav-arrow-btn' onclick='changeWeek(1)'>SEMANA SIGUIENTE ▶</button>");
            sb.append("</div>");

            sb.append("<div class='week-pill-scroller'>");
            for (int s = 1; s <= 16; s++) {
                sb.append("<div class='week-pill ").append(s == 1 ? "active-pill" : "").append("' onclick='selectWeek(").append(s).append(")' id='pill-").append(s).append("'>SEM ").append(s < 10 ? "0" + s : s).append("</div>");
            }
            sb.append("</div>");

            // Paneles de Semanas con doble compartimento y Títulos Adaptables
            for (int s = 1; s <= 16; s++) {
                final int currentWeek = s;
                List<Clase> clasesSemana = todasLasClases.stream()
                        .filter(c -> Objects.equals(c.getSemanaId(), currentWeek))
                        .collect(Collectors.toList());

                // Si Flor subió un tema propio para esta semana, mostramos el tema que ella escribió!
                String tituloSemana = (s <= temasSemanas.length) ? temasSemanas[s-1] : "Semana " + s;
                if (!clasesSemana.isEmpty() && clasesSemana.get(0).getTitulo() != null && !clasesSemana.get(0).getTitulo().trim().isEmpty()) {
                    tituloSemana = clasesSemana.get(0).getTitulo();
                }

                sb.append("<div class='week-card ").append(s == 1 ? "active-week" : "").append("' id='week-card-").append(s).append("'>");
                sb.append("  <div class='week-card-header'>");
                sb.append("    <div>");
                sb.append("      <span class='week-badge-lg'>SEMANA ").append(s < 10 ? "0" + s : s).append(" DE 16</span>");
                sb.append("      <h2 class='week-card-title'>").append(tituloSemana).append("</h2>");
                sb.append("    </div>");
                sb.append("  </div>");

                sb.append("  <div class='week-dual-grid'>");

                // Compartimento 1: Material de Clase
                sb.append("    <div class='compartment'>");
                sb.append("      <div class='compartment-header'>");
                sb.append("        <div class='comp-title comp-mat'>📚 Material Oficial de Clase</div>");
                sb.append("      </div>");

                List<Clase> matList = new ArrayList<>();
                List<Clase> tarList = new ArrayList<>();

                for (Clase c : clasesSemana) {
                    List<Archivo> archs = todosLosArchivos.stream()
                            .filter(a -> Objects.equals(a.getClaseId(), c.getId()))
                            .collect(Collectors.toList());
                    boolean esTarea = archs.stream().anyMatch(a -> "TAREA".equalsIgnoreCase(a.getTipo()));
                    if (esTarea) tarList.add(c);
                    else matList.add(c);
                }

                if (matList.isEmpty()) {
                    sb.append("      <div class='empty-slot'>📭 Sin material de clase registrado en MySQL</div>");
                } else {
                    for (Clase c : matList) {
                        List<Archivo> archs = todosLosArchivos.stream()
                                .filter(a -> Objects.equals(a.getClaseId(), c.getId()))
                                .collect(Collectors.toList());
                        sb.append("      <div class='doc-card'>");
                        sb.append("        <div class='doc-title'>").append(c.getTitulo()).append("</div>");
                        sb.append("        <div class='doc-desc'>").append(c.getDescripcion()).append("</div>");
                        sb.append("        <div class='doc-actions'>");
                        for (Archivo arc : archs) {
                            String url = "/archivos/descargar/" + arc.getId();
                            sb.append("          <a href='").append(url).append("' class='btn-down' target='_blank'>📥 Descargar</a>");
                            if (arc.getNombreOriginal() != null && arc.getNombreOriginal().toLowerCase().endsWith(".pdf")) {
                                sb.append("          <button class='btn-view' onclick=\"openPdfModal('").append(url).append("', '").append(arc.getNombreOriginal()).append("')\">👁️ Ver PDF</button>");
                            }
                        }
                        if (esAdmin) {
                            sb.append("          <a href='/archivos/eliminar/").append(c.getId()).append("' class='btn-del' onclick=\"return confirm('¿Eliminar este material de MySQL?');\">🗑️</a>");
                        }
                        sb.append("        </div>");
                        sb.append("      </div>");
                    }
                }
                sb.append("    </div>");

                // Compartimento 2: Tareas Desarrolladas
                sb.append("    <div class='compartment'>");
                sb.append("      <div class='compartment-header'>");
                sb.append("        <div class='comp-title comp-tar'>📝 Tareas & Prácticas Desarrolladas</div>");
                sb.append("      </div>");

                if (tarList.isEmpty()) {
                    sb.append("      <div class='empty-slot'>📭 Sin tareas desarrolladas registradas aún</div>");
                } else {
                    for (Clase c : tarList) {
                        List<Archivo> archs = todosLosArchivos.stream()
                                .filter(a -> Objects.equals(a.getClaseId(), c.getId()))
                                .collect(Collectors.toList());
                        sb.append("      <div class='doc-card' style='border-color:rgba(255,0,127,0.35);'>");
                        sb.append("        <div class='doc-title' style='color:#ff80bf;'>").append(c.getTitulo()).append("</div>");
                        sb.append("        <div class='doc-desc'>").append(c.getDescripcion()).append("</div>");
                        sb.append("        <div class='doc-actions'>");
                        for (Archivo arc : archs) {
                            String url = "/archivos/descargar/" + arc.getId();
                            sb.append("          <a href='").append(url).append("' class='btn-down' style='border-color:#ff007f; color:#ff66b2;' target='_blank'>📥 Descargar Tarea</a>");
                            if (arc.getNombreOriginal() != null && arc.getNombreOriginal().toLowerCase().endsWith(".pdf")) {
                                sb.append("          <button class='btn-view' onclick=\"openPdfModal('").append(url).append("', '").append(arc.getNombreOriginal()).append("')\">👁️ Ver PDF</button>");
                            }
                        }
                        if (esAdmin) {
                            sb.append("          <a href='/archivos/eliminar/").append(c.getId()).append("' class='btn-del' onclick=\"return confirm('¿Eliminar esta tarea de MySQL?');\">🗑️</a>");
                        }
                        sb.append("        </div>");
                        sb.append("      </div>");
                    }
                }
                sb.append("    </div>");

                sb.append("  </div>");
                sb.append("</div>");
            }
        }

        // =======================================================
        // TAB 03: CONTACTO & REDES (SIN FALSO QR NI CÓDIGO DE BARRAS)
        // =======================================================
        if ("contacto".equals(activeTab)) {
            sb.append("<div class='contact-hub-grid'>");

            // Carnet Estudiantil Holográfico Oficial de Flor (Limpio y Elegante)
            sb.append("  <div>");
            sb.append("    <div class='student-id-card'>");
            sb.append("      <div class='id-header'>");
            sb.append("        <div style='display:flex; align-items:center; gap:10px;'>");
            sb.append("          <img src='/image.png' alt='UPLA' style='height:38px; filter:drop-shadow(0 0 10px #ff007f);' onerror=\"this.src='https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Logo_Universidad_Peruana_Los_Andes.png/640px-Logo_Universidad_Peruana_Los_Andes.png'\">");
            sb.append("          <div>");
            sb.append("            <div style='font-size:12.5px; font-weight:900; color:#fff;'>UNIVERSIDAD PERUANA LOS ANDES</div>");
            sb.append("            <div style='font-size:10px; font-family:\"Fira Code\",monospace; color:#00f3ff;'>FACULTAD DE INGENIERÍA • EPISC</div>");
            sb.append("          </div>");
            sb.append("        </div>");
            sb.append("        <div class='id-chip'></div>");
            sb.append("      </div>");

            sb.append("      <div class='id-body'>");
            sb.append("        <div class='id-photo'>");
            sb.append("          🌸");
            sb.append("          <div class='id-photo-badge'>ESTUDIANTE</div>");
            sb.append("        </div>");
            sb.append("        <div class='id-details'>");
            sb.append("          <div class='id-name'>").append(nombreAlumna).append("</div>");
            sb.append("          <div class='id-spec'>Ingeniería de Sistemas y Computación</div>");
            sb.append("          <div class='id-code'>CORREO: <b style='color:#00f3ff;'>s01269h@upla.edu.pe</b></div>");
            sb.append("          <div class='id-code' style='margin-top:4px;'>ESTUDIANTE TITULAR • 2026-I</div>");
            sb.append("        </div>");
            sb.append("      </div>");

            sb.append("      <div style='background:rgba(255,255,255,0.035); border:1px solid rgba(216,132,255,0.22); border-radius:12px; padding:12px; font-size:12px; line-height:1.5; color:#d8c6f2;'>");
            sb.append("        📍 <b>Campus Chorrillos:</b> Av. Giráldez 230 / Av. Ferrocarril, Huancayo - Perú.<br>");
            sb.append("        🏛️ <b>Pabellón:</b> Facultad de Ingeniería • EPISC.");
            sb.append("      </div>");

            // Sello de Certificación Digital (Sin falso QR)
            sb.append("      <div class='digital-cert-seal'>");
            sb.append("        <div style='font-family:\"Fira Code\",monospace; font-size:11.5px; font-weight:800; color:#00f3ff;'>🛡️ ACREDITACIÓN DIGITAL UNIVERSITARIA // EPISC UPLA</div>");
            sb.append("        <div style='font-family:\"Fira Code\",monospace; font-size:9.5px; color:#d884ff; margin-top:4px;'>FIRMA CRIPTOGRÁFICA VERIFICADA • HASH SHA-256: 332181-UPLA-2026</div>");
            sb.append("      </div>");
            sb.append("    </div>");
            sb.append("  </div>");

            // Enlaces de Comunicación Institucional
            sb.append("  <div>");
            sb.append("    <div class='cyber-card'>");
            sb.append("      <span class='neon-sub'>// COMUNICACIÓN DIRECTA & CÁTEDRA</span>");
            sb.append("      <h2 class='neon-title' style='font-size:22px; margin:6px 0 16px;'>Canales Oficiales</h2>");
            
            sb.append("      <div class='contact-links-grid'>");

            // Correo Institucional de Flor
            sb.append("        <a href='mailto:s01269h@upla.edu.pe' class='social-card'>");
            sb.append("          <div class='social-head'>");
            sb.append("            <div class='social-icon' style='color:#00f3ff;'>✉️</div>");
            sb.append("            <div><div class='social-title'>Correo de la Autora</div><div class='social-tag'>Buzón UPLA</div></div>");
            sb.append("          </div>");
            sb.append("          <div class='social-desc'>Escríbeme directamente a <b>s01269h@upla.edu.pe</b> para consultas o revisiones.</div>");
            sb.append("          <div class='social-action'>Abrir Correo ➔</div>");
            sb.append("        </a>");

            // Correo del Ingeniero Docente
            sb.append("        <a href='mailto:d.rfernandezb@ms.upla.edu.pe' class='social-card'>");
            sb.append("          <div class='social-head'>");
            sb.append("            <div class='social-icon' style='color:#d884ff;'>👨🏫</div>");
            sb.append("            <div><div class='social-title'>Docente de Cátedra</div><div class='social-tag'>Mg. Raúl Fernández</div></div>");
            sb.append("          </div>");
            sb.append("          <div class='social-desc'>Correo de contacto del docente: <b>d.rfernandezb@ms.upla.edu.pe</b>.</div>");
            sb.append("          <div class='social-action'>Contactar Docente ➔</div>");
            sb.append("        </a>");

            // Repositorio Académico
            sb.append("        <a href='https://github.com' target='_blank' class='social-card'>");
            sb.append("          <div class='social-head'>");
            sb.append("            <div class='social-icon'>🐙</div>");
            sb.append("            <div><div class='social-title'>GitHub Académico</div><div class='social-tag'>Control de Versiones</div></div>");
            sb.append("          </div>");
            sb.append("          <div class='social-desc'>Repositorio de código fuente en Java 21, Spring Boot y MySQL.</div>");
            sb.append("          <div class='social-action'>Ver Repositorio ➔</div>");
            sb.append("        </a>");

            // Ubicación del Campus Huancayo
            sb.append("        <div class='social-card'>");
            sb.append("          <div class='social-head'>");
            sb.append("            <div class='social-icon' style='color:#00ff88;'>📍</div>");
            sb.append("            <div><div class='social-title'>Campus Universitario</div><div class='social-tag' style='color:#00ff88;'>Huancayo, Junín</div></div>");
            sb.append("          </div>");
            sb.append("          <div class='social-desc'>Facultad de Ingeniería • Escuela Profesional de Ingeniería de Sistemas.</div>");
            sb.append("          <div class='social-action' style='color:#00ff88;'>Sede Central UPLA</div>");
            sb.append("        </div>");

            sb.append("      </div>");

            // Buzón Directo
            sb.append("      <div style='margin-top:24px; padding-top:20px; border-top:1px dashed rgba(216,132,255,0.25);'>");
            sb.append("        <span class='neon-sub'>// TRANSMISIÓN DIRECTA AL BUZÓN INSTITUCIONAL</span>");
            sb.append("        <form action='/contacto/enviar' method='POST' style='margin-top:12px; display:flex; gap:10px;'>");
            sb.append("          <input type='text' name='mensaje' class='cyber-input' style='margin:0;' placeholder='Escribe un mensaje institucional para Flor...' required>");
            sb.append("          <button type='submit' class='submit-btn' style='margin:0; width:auto; padding:0 24px;'>ENVIAR</button>");
            sb.append("        </form>");
            sb.append("      </div>");

            sb.append("    </div>");
            sb.append("  </div>");

            sb.append("</div>");
        }

        sb.append("</main>");

        // ==========================================================
        // ANIMALITO COMPLETO: MICHI 🐱🐾
        // ==========================================================
        sb.append("<div id='cyberCatContainer' onclick='toggleMichi()'>");
        sb.append("  <div class='cat-bubble'>🐾 ¡Miau! ¿Tienes dudas? ¡Tócame!</div>");
        sb.append("  <svg class='cat-sprite' viewBox='0 0 100 90' fill='none' xmlns='http://www.w3.org/2000/svg'>");
        sb.append("    <path class='cat-tail' d='M 30 65 Q 10 50 15 35 Q 20 20 10 15 Q 5 25 10 40 Q 15 60 30 70 Z' fill='url(#catGrad)' stroke='#00f3ff' stroke-width='1.5'/>");
        sb.append("    <ellipse class='cat-paw-back' cx='35' cy='74' rx='10' ry='6' fill='#d884ff' stroke='#ff007f' stroke-width='1.5'/>");
        sb.append("    <ellipse class='cat-paw-back' cx='68' cy='74' rx='10' ry='6' fill='#d884ff' stroke='#ff007f' stroke-width='1.5'/>");
        sb.append("    <ellipse cx='50' cy='58' rx='28' ry='22' fill='url(#catGrad)' stroke='#d884ff' stroke-width='2'/>");
        sb.append("    <path d='M 32 46 Q 50 56 68 46' stroke='#ff007f' stroke-width='3' stroke-linecap='round'/>");
        sb.append("    <circle cx='50' cy='52' r='4' fill='#00f3ff' filter='drop-shadow(0 0 5px #00f3ff)'/>");
        sb.append("    <g class='cat-ear-left'>");
        sb.append("      <polygon points='30,30 22,8 42,20' fill='url(#catGrad)' stroke='#ff007f' stroke-width='1.5'/>");
        sb.append("      <polygon points='31,27 26,13 39,21' fill='#ff80df'/>");
        sb.append("    </g>");
        sb.append("    <g class='cat-ear-right'>");
        sb.append("      <polygon points='70,30 78,8 58,20' fill='url(#catGrad)' stroke='#ff007f' stroke-width='1.5'/>");
        sb.append("      <polygon points='69,27 74,13 61,21' fill='#ff80df'/>");
        sb.append("    </g>");
        sb.append("    <circle cx='50' cy='32' r='22' fill='url(#catGrad)' stroke='#d884ff' stroke-width='2'/>");
        sb.append("    <g class='cat-eye'>");
        sb.append("      <ellipse cx='42' cy='30' rx='4.5' ry='6' fill='#00f3ff' filter='drop-shadow(0 0 6px #00f3ff)'/>");
        sb.append("      <ellipse cx='58' cy='30' rx='4.5' ry='6' fill='#00f3ff' filter='drop-shadow(0 0 6px #00f3ff)'/>");
        sb.append("      <circle cx='43.5' cy='28.5' r='1.8' fill='#fff'/>");
        sb.append("      <circle cx='59.5' cy='28.5' r='1.8' fill='#fff'/>");
        sb.append("    </g>");
        sb.append("    <polygon points='50,37 47,34 53,34' fill='#ff80df'/>");
        sb.append("    <path d='M 47 38 Q 50 41 53 38' stroke='#fff' stroke-width='1.2' stroke-linecap='round'/>");
        sb.append("    <path d='M 36 34 L 20 31 M 36 37 L 18 38 M 64 34 L 80 31 M 64 37 L 82 38' stroke='#d884ff' stroke-width='1.2' stroke-linecap='round'/>");
        sb.append("    <ellipse class='cat-paw-front' cx='42' cy='75' rx='7' ry='5' fill='#ff80df' stroke='#fff' stroke-width='1.2'/>");
        sb.append("    <ellipse class='cat-paw-front' cx='58' cy='75' rx='7' ry='5' fill='#ff80df' stroke='#fff' stroke-width='1.2'/>");
        sb.append("    <defs>");
        sb.append("      <linearGradient id='catGrad' x1='0%' y1='0%' x2='100%' y2='100%'>");
        sb.append("        <stop offset='0%' stop-color='#d884ff'/>");
        sb.append("        <stop offset='60%' stop-color='#ff007f'/>");
        sb.append("        <stop offset='100%' stop-color='#3b0754'/>");
        sb.append("      </linearGradient>");
        sb.append("    </defs>");
        sb.append("  </svg>");
        sb.append("</div>");

        // Ventana de Chat de MICHI
        sb.append("<div id='michiWindow'>");
        sb.append("  <div class='michi-head'>");
        sb.append("    <div class='michi-title'>🐾 MICHI // ASISTENTE DEL PORTAFOLIO</div>");
        sb.append("    <button onclick='toggleMichi()' style='background:transparent; border:none; color:#ff6688; font-size:16px; cursor:pointer; font-weight:900;'>✕</button>");
        sb.append("  </div>");
        sb.append("  <div class='michi-body' id='michiMessages'>");
        sb.append("    <div class='michi-msg msg-bot'>¡Miau! 🐾 Hola Flor, soy <b>Michi</b>, tu asistente del portafolio.<br><br>Sé <b>TODO</b> sobre este curso: el docente titular Mg. Raúl Fernández, tus datos como autora, las 16 semanas, el sílabo, la persistencia en MySQL 8.0 y el despliegue en la nube. ¿Qué deseas consultar?</div>");
        sb.append("  </div>");
        sb.append("  <div class='michi-chips'>");
        sb.append("    <div class='m-chip' onclick=\"askMichi('¿Quién es el docente?')\">👨🏫 El Ingeniero Docente</div>");
        sb.append("    <div class='m-chip' onclick=\"askMichi('¿Quién es la autora?')\">🌸 Alumna Titular</div>");
        sb.append("    <div class='m-chip' onclick=\"askMichi('¿Cómo sé si se guardó en MySQL?')\">🗄️ ¿Se guardó en MySQL?</div>");
        sb.append("    <div class='m-chip' onclick=\"askMichi('¿Qué vemos en Semana 4?')\">📐 Semana 4 (Vistas 4+1)</div>");
        sb.append("    <div class='m-chip' onclick=\"askMichi('¿Qué es la sumilla del curso?')\">📖 Sumilla del Sílabo</div>");
        sb.append("  </div>");
        sb.append("  <div class='michi-input-box'>");
        sb.append("    <input type='text' id='michiInput' class='michi-input' placeholder='Escribe cualquier pregunta para Michi...' onkeydown='if(event.key===\"Enter\") sendMichi();'>");
        sb.append("    <button class='michi-send-btn' onclick='sendMichi()'>ENVIAR</button>");
        sb.append("  </div>");
        sb.append("</div>");

        // Modal PDF
        sb.append("<div id='pdfModal' class='cyber-modal' style='display:none;'>");
        sb.append("  <div class='modal-dialog'>");
        sb.append("    <div class='modal-header'>");
        sb.append("      <div class='modal-title' id='pdfTitle'>DOCUMENTO ACADÉMICO</div>");
        sb.append("      <button class='modal-close' onclick=\"document.getElementById('pdfModal').style.display='none';\">CERRAR [X]</button>");
        sb.append("    </div>");
        sb.append("    <iframe id='pdfFrame' src='' style='width:100%; height:70vh; border:none; border-radius:14px; background:#fff;'></iframe>");
        sb.append("  </div>");
        sb.append("</div>");

        // Modal Editar Nombre
        if (esAdmin) {
            sb.append("<div id='editModal' class='cyber-modal' style='display:none;'>");
            sb.append("  <div class='modal-dialog' style='max-width:500px;'>");
            sb.append("    <div class='modal-header'>");
            sb.append("      <div class='modal-title'>✏️ EDITAR NOMBRE DE LA ALUMNA TITULAR</div>");
            sb.append("      <button class='modal-close' onclick=\"document.getElementById('editModal').style.display='none';\">✕</button>");
            sb.append("    </div>");
            sb.append("    <form action='/perfil/actualizar' method='POST'>");
            sb.append("      <label style='font-size:11.5px; font-family:\"Fira Code\",monospace; color:#d884ff; display:block; margin-bottom:6px; font-weight:700;'>NOMBRE COMPLETO:</label>");
            sb.append("      <input type='text' name='nombre' class='cyber-input' value='").append(nombreAlumna).append("' required>");
            sb.append("      <button type='submit' class='submit-btn' style='margin-top:14px;'>GUARDAR EN MYSQL</button>");
            sb.append("    </form>");
            sb.append("  </div>");
            sb.append("</div>");
        }

        // Scripts interactivos
        sb.append("<script>");
        sb.append("  let curWeek = 1;");
        sb.append("  function selectWeek(n) {");
        sb.append("    curWeek = n;");
        sb.append("    document.querySelectorAll('.week-card').forEach(el => el.classList.remove('active-week'));");
        sb.append("    document.querySelectorAll('.week-pill').forEach(el => el.classList.remove('active-pill'));");
        sb.append("    const target = document.getElementById('week-card-' + n);");
        sb.append("    const pill = document.getElementById('pill-' + n);");
        sb.append("    if(target) target.classList.add('active-week');");
        sb.append("    if(pill) { pill.classList.add('active-pill'); pill.scrollIntoView({ behavior:'smooth', inline:'center', block:'nearest' }); }");
        sb.append("    const status = document.getElementById('carouselStatus');");
        sb.append("    if(status) status.innerText = 'NAVEGANDO SEMANA ' + (n < 10 ? '0' + n : n) + ' DE 16';");
        // AQUÍ: Llena el campo de texto libre con el número de semana si haces clic en el carrusel
        sb.append("    const inp = document.getElementById('formSemanaInput');");
        sb.append("    if(inp) inp.value = n;");
        sb.append("  }");
        sb.append("  function changeWeek(d) {");
        sb.append("    let next = curWeek + d;");
        sb.append("    if(next < 1) next = 16;");
        sb.append("    if(next > 16) next = 1;");
        sb.append("    selectWeek(next);");
        sb.append("  }");

        // Lógica de Michi
        sb.append("  function toggleMichi() {");
        sb.append("    const w = document.getElementById('michiWindow');");
        sb.append("    w.style.display = (w.style.display === 'flex') ? 'none' : 'flex';");
        sb.append("    if(w.style.display === 'flex') document.getElementById('michiInput').focus();");
        sb.append("  }");
        sb.append("  function askMichi(q) {");
        sb.append("    document.getElementById('michiInput').value = q;");
        sb.append("    sendMichi();");
        sb.append("  }");
        sb.append("  function sendMichi() {");
        sb.append("    const input = document.getElementById('michiInput');");
        sb.append("    const text = input.value.trim();");
        sb.append("    if(!text) return;");
        sb.append("    const box = document.getElementById('michiMessages');");
        sb.append("    box.innerHTML += `<div class='michi-msg msg-user'>${text}</div>`;");
        sb.append("    input.value = '';");
        sb.append("    box.scrollTop = box.scrollHeight;");
        sb.append("    setTimeout(() => {");
        sb.append("      let resp = '¡Miau! 🐾 No tengo esa consulta exacta, pero puedes revisar las 16 semanas en el carrusel o consultar el correo institucional de Flor.';");
        sb.append("      const t = text.toLowerCase();");
        sb.append("      if(t.includes('docente') || t.includes('profesor') || t.includes('raul') || t.includes('raúl') || t.includes('ing')) {");
        sb.append("        resp = '👨🏫 El docente titular de la cátedra de Arquitectura de Software es el <b>Mg. Raúl Enrique Fernández Bejarano</b>. Su correo oficial es <b>d.rfernandezb@ms.upla.edu.pe</b> y el semestre va del 06 de Abril al 26 de Julio de 2026.';");
        sb.append("      } else if(t.includes('autora') || t.includes('flor') || t.includes('estudiante') || t.includes('quién es') || t.includes('creadora')) {");
        sb.append("        resp = '🌸 La autora y titular de este portafolio es <b>Flor Xiomara Medina Salazar</b>, estudiante de la Escuela Profesional de Ingeniería de Sistemas y Computación (EPISC - UPLA). Correo: <b>s01269h@upla.edu.pe</b>.';");
        sb.append("      } else if(t.includes('mysql') || t.includes('base de datos') || t.includes('guardar') || t.includes('guardo')) {");
        sb.append("        resp = '🗄️ <b>¿Cómo sabemos que se guarda en MySQL?</b><br>¡Miau! Cada vez que subes un archivo o clase, Spring Data JPA ejecuta un <code>INSERT INTO clases</code> y <code>INSERT INTO archivos</code> en MySQL 8.0 (InnoDB). Puedes ver el contador en vivo en el encabezado superior o abrir MySQL Workbench y correr <code>SELECT * FROM clases;</code>.';");
        sb.append("      } else if(t.includes('github') || t.includes('git')) {");
        sb.append("        resp = '🐙 <b>¿Cómo subir a GitHub?</b><br>1. Abre tu terminal en la carpeta del proyecto.<br>2. Ejecuta <code>git init</code>, luego <code>git add .</code>.<br>3. Haz el commit: <code>git commit -m \"Subiendo portafolio 2026-I\"</code>.<br>4. Conecta tu repositorio remoto con <code>git remote add origin https://github.com/TU_USUARIO/TU_REPO.git</code>.<br>5. Envía tus cambios con <code>git push -u origin main</code>.';");
        sb.append("      } else if(t.includes('azure') || t.includes('nube') || t.includes('ansure')) {");
        sb.append("        resp = '☁️ <b>¿Cómo desplegar en Microsoft Azure?</b><br>1. Empaqueta tu aplicación con <code>mvn clean package</code> para generar el archivo <code>.jar</code>.<br>2. En el portal de Azure, crea un recurso de <b>Azure App Service (Java 21)</b>.<br>3. Crea una base de datos <b>Azure Database for MySQL Flexible Server</b> y ajusta las credenciales en <code>application.properties</code>.<br>4. Despliega el JAR directamente desde VS Code con la extensión Azure App Service o usando GitHub Actions.';");
        sb.append("      } else if(t.includes('sumilla') || t.includes('competencia') || t.includes('silabo') || t.includes('sílabo')) {");
        sb.append("        resp = '📖 <b>Sumilla del Curso:</b> Asignatura práctica orientada a formular soluciones arquitectónicas bajo normas IEEE 1471 e ISO/IEC 25010, POO, modelos 4+1 vistas de Kruchten y frameworks empresariales Spring Boot.';");
        sb.append("      } else if(t.includes('semana 4') || t.includes('4+1')) {");
        sb.append("        resp = '📐 <b>Semana 04:</b> Modelo de 4+1 Vistas de Philippe Kruchten (Lógica, Desarrollo, Procesos, Física y Casos de Uso) con diagramas de despliegue.';");
        sb.append("      } else if(t.includes('correo') || t.includes('email')) {");
        sb.append("        resp = '📧 El correo oficial de Flor Xiomara es <b>s01269h@upla.edu.pe</b> y el del docente es <b>d.rfernandezb@ms.upla.edu.pe</b>.';");
        sb.append("      } else if(t.includes('upla') || t.includes('universidad')) {");
        sb.append("        resp = '🏛️ <b>Universidad Peruana Los Andes (UPLA)</b>, Facultad de Ingeniería, Escuela Profesional de Ingeniería de Sistemas y Computación (EPISC). Campus Chorrillos, Huancayo, Perú.';");
        sb.append("      } else if(t.includes('tarea') || t.includes('subir')) {");
        sb.append("        resp = '📝 En la pestaña <b>02. Portafolio Semanal & Tareas</b> puedes subir archivos seleccionando si es <i>Material de Clase</i> o <i>Tarea Desarrollada</i>.';");
        sb.append("      }");
        sb.append("      box.innerHTML += `<div class='michi-msg msg-bot'>${resp}</div>`;");
        sb.append("      box.scrollTop = box.scrollHeight;");
        sb.append("    }, 380);");
        sb.append("  }");

        // PDF Visor
        sb.append("  function openPdfModal(url, title) {");
        sb.append("    document.getElementById('pdfTitle').innerText = 'DOCUMENTO: ' + title;");
        sb.append("    document.getElementById('pdfFrame').src = url;");
        sb.append("    document.getElementById('pdfModal').style.display = 'flex';");
        sb.append("  }");

        // Lienzo de Estrellas, Meteoritos y Estela de Cometa en el Cursor (Estrella Fugaz)
        sb.append("  const c = document.getElementById('bgCanvas'), cx = c.getContext('2d');");
        sb.append("  let W = c.width = window.innerWidth, H = c.height = window.innerHeight;");
        sb.append("  window.onresize = () => { W = c.width = window.innerWidth; H = c.height = window.innerHeight; };");
        
        // 110 Estrellas titilantes
        sb.append("  const stars = [];");
        sb.append("  for(let i=0; i<110; i++) {");
        sb.append("    stars.push({ x:Math.random()*W, y:Math.random()*H, r:Math.random()*2+0.8, vx:(Math.random()-0.5)*0.35, vy:(Math.random()-0.5)*0.35, alpha:Math.random(), dAlpha:(Math.random()*0.02+0.005)*(Math.random()>0.5?1:-1) });");
        sb.append("  }");

        // Meteoritos / Estrellas Fugaces periódicos en el cielo
        sb.append("  const meteors = [];");
        sb.append("  function spawnMeteor() {");
        sb.append("    meteors.push({");
        sb.append("      x: Math.random()*W*1.2,");
        sb.append("      y: Math.random()*(H*0.4),");
        sb.append("      len: Math.random()*130+90,");
        sb.append("      speed: Math.random()*9+12,");
        sb.append("      angle: Math.PI/4 + (Math.random()-0.5)*0.2,");
        sb.append("      life: 1.0,");
        sb.append("      decay: Math.random()*0.025+0.015,");
        sb.append("      width: Math.random()*2.8+1.5");
        sb.append("    });");
        sb.append("  }");
        sb.append("  setInterval(() => { if(Math.random()<0.7) spawnMeteor(); }, 1500);");

        // FÍSICA DE ESTRELLA FUGAZ / COMETA EN EL CURSOR DEL MOUSE
        sb.append("  const cometTrail = [];");
        sb.append("  let mouse = { x: -1000, y: -1000 };");
        sb.append("  window.addEventListener('mousemove', (e) => {");
        sb.append("    const dx = e.clientX - mouse.x, dy = e.clientY - mouse.y; const speed = Math.hypot(dx, dy);");
        sb.append("    mouse.x = e.clientX; mouse.y = e.clientY;");
        sb.append("    const count = Math.min(Math.floor(speed / 3) + 2, 8);");
        sb.append("    for(let i=0; i<count; i++) {");
        sb.append("      cometTrail.push({");
        sb.append("        x: mouse.x + (Math.random() - 0.5) * 6,");
        sb.append("        y: mouse.y + (Math.random() - 0.5) * 6,");
        sb.append("        vx: -dx * 0.12 + (Math.random() - 0.5) * 2,");
        sb.append("        vy: -dy * 0.12 + (Math.random() - 0.5) * 2,");
        sb.append("        r: Math.random() * 3.5 + 1.2,");
        sb.append("        alpha: 1,");
        sb.append("        decay: Math.random() * 0.035 + 0.02,");
        sb.append("        color: Math.random() > 0.5 ? '#00f3ff' : (Math.random() > 0.5 ? '#ff007f' : '#d884ff')");
        sb.append("      });");
        sb.append("    }");
        sb.append("  });");

        sb.append("  function bgLoop() {");
        sb.append("    cx.clearRect(0, 0, W, H);");
        
        // Dibujar estrellas de fondo
        sb.append("    for(let i=0; i<stars.length; i++) {");
        sb.append("      let s = stars[i]; s.x += s.vx; s.y += s.vy; s.alpha += s.dAlpha;");
        sb.append("      if(s.alpha<=0.1 || s.alpha>=1) s.dAlpha *= -1;");
        sb.append("      if(s.x<0 || s.x>W) s.vx *= -1; if(s.y<0 || s.y>H) s.vy *= -1;");
        sb.append("      cx.beginPath(); cx.arc(s.x, s.y, s.r, 0, Math.PI*2); cx.fillStyle = `rgba(216,132,255,${s.alpha*0.85})`; cx.fill();");
        sb.append("      for(let j=i+1; j<stars.length; j++) {");
        sb.append("        let s2 = stars[j], d = Math.hypot(pX=s.x-s2.x, pY=s.y-s2.y);");
        sb.append("        if(d < 105) {");
        sb.append("          cx.beginPath(); cx.moveTo(s.x, s.y); cx.lineTo(s2.x, s2.y);");
        sb.append("          cx.strokeStyle = `rgba(216,132,255,${0.25*(1-d/105)})`; cx.lineWidth = 0.7; cx.stroke();");
        sb.append("        }");
        sb.append("      }");
        sb.append("    }");

        // Dibujar meteoritos
        sb.append("    for(let i=meteors.length-1; i>=0; i--) {");
        sb.append("      let m = meteors[i];");
        sb.append("      let tailX = m.x - Math.cos(m.angle)*m.len;");
        sb.append("      let tailY = m.y - Math.sin(m.angle)*m.len;");
        sb.append("      let grad = cx.createLinearGradient(m.x, m.y, tailX, tailY);");
        sb.append("      grad.addColorStop(0, `rgba(0, 243, 255, ${m.life})`);");
        sb.append("      grad.addColorStop(0.35, `rgba(216, 132, 255, ${m.life*0.85})`);");
        sb.append("      grad.addColorStop(1, 'transparent');");
        sb.append("      cx.beginPath(); cx.moveTo(m.x, m.y); cx.lineTo(tailX, tailY);");
        sb.append("      cx.strokeStyle = grad; cx.lineWidth = m.width*m.life; cx.lineCap = 'round'; cx.stroke();");
        sb.append("      m.x += Math.cos(m.angle)*m.speed; m.y += Math.sin(m.angle)*m.speed; m.life -= m.decay;");
        sb.append("      if(m.life <= 0) meteors.splice(i, 1);");
        sb.append("    }");

        // Dibujar estela de estrella fugaz en el ratón
        sb.append("    for(let i=cometTrail.length-1; i>=0; i--) {");
        sb.append("      const p = cometTrail[i]; p.x += p.vx; p.y += p.vy; p.alpha -= p.decay; p.r *= 0.96;");
        sb.append("      if(p.alpha <= 0) { cometTrail.splice(i, 1); continue; }");
        sb.append("      cx.save(); cx.shadowBlur=14; cx.shadowColor=p.color; cx.fillStyle=p.color; cx.globalAlpha=p.alpha;");
        sb.append("      cx.beginPath(); cx.arc(p.x, p.y, p.r, 0, Math.PI*2); cx.fill(); cx.restore();");
        sb.append("    }");

        sb.append("    requestAnimationFrame(bgLoop);");
        sb.append("  }");
        sb.append("  bgLoop();");

        // Cuenta del Loader 1-100%
        if (justLoggedIn) {
            sb.append("  let count = 0;");
            sb.append("  const pctEl = document.getElementById('loaderPct');");
            sb.append("  const fillEl = document.getElementById('loaderFill');");
            sb.append("  const statEl = document.getElementById('loaderStatus');");
            sb.append("  const welcEl = document.getElementById('loaderWelcome');");
            sb.append("  const ldrEl = document.getElementById('cyberLoader');");
            sb.append("  const timer = setInterval(() => {");
            sb.append("    count++;");
            sb.append("    pctEl.innerText = (count < 10 ? '00' : (count < 100 ? '0' : '')) + count + '%';");
            sb.append("    fillEl.style.width = count + '%';");
            sb.append("    if(count === 30) statEl.innerText = 'CONECTANDO DRIVER MYSQL 8.0 INNODB...';");
            sb.append("    if(count === 65) statEl.innerText = 'CARGANDO 16 SEMANAS Y VISTAS ARQUITECTÓNICAS...';");
            sb.append("    if(count === 90) statEl.innerText = 'INICIALIZANDO MOTOR CIBERNÉTICO DE FLOR XIOMARA...';");
            sb.append("    if(count >= 100) {");
            sb.append("      clearInterval(timer);");
            sb.append("      statEl.style.display = 'none';");
            sb.append("      welcEl.style.display = 'block';");
            sb.append("      setTimeout(() => {");
            sb.append("        ldrEl.style.opacity = '0';");
            sb.append("        setTimeout(() => { ldrEl.style.display = 'none'; }, 700);");
            sb.append("      }, 900);");
            sb.append("    }");
            sb.append("  }, 22);");
        }

        sb.append("</script>");
        sb.append("</body></html>");

        return sb.toString();
    }

    // ==========================================================
    // 3. PERSISTENCIA EN MYSQL (CLASES, TAREAS Y ARCHIVOS)
    // ==========================================================

    @PostMapping("/clases/crear")
    public String crearClase(@RequestParam("semana") String semanaStr,
                             @RequestParam("titulo") String titulo,
                             @RequestParam("descripcion") String descripcion,
                             @RequestParam("tipo") String tipo,
                             @RequestParam("archivo") MultipartFile file,
                             HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.esAdmin()) {
            return "redirect:/login";
        }

        // Extracción inteligente de la semana (acepta números o textos como "Semana 3")
        int semana = 1;
        try {
            String soloNum = semanaStr.replaceAll("[^0-9]", "");
            if (!soloNum.isEmpty()) {
                semana = Integer.parseInt(soloNum);
            }
        } catch (Exception ignored) {
            semana = 1;
        }

        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        Clase nuevaClase = new Clase(semana, titulo, descripcion, fecha);
        claseRepo.save(nuevaClase);

        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String serverFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                Files.copy(file.getInputStream(), this.rootLocation.resolve(serverFilename), StandardCopyOption.REPLACE_EXISTING);

                Archivo nuevoArchivo = new Archivo(nuevaClase.getId(), originalFilename, serverFilename, tipo, fecha);
                archivoRepo.save(nuevoArchivo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "redirect:/portafolio?tab=archivos&msg=upload_ok";
    }

    @GetMapping("/archivos/descargar/{id}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable("id") Integer id) {
        Optional<Archivo> arcOpt = archivoRepo.findById(id);
        if (arcOpt.isPresent()) {
            Archivo arc = arcOpt.get();
            try {
                Path file = rootLocation.resolve(arc.getNombreServidor());
                Resource resource = new UrlResource(file.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + arc.getNombreOriginal() + "\"")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(resource);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/archivos/eliminar/{claseId}")
    public String eliminarClase(@PathVariable("claseId") Integer claseId, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null && usuario.esAdmin()) {
            List<Archivo> archivos = archivoRepo.findByClaseId(claseId);
            for (Archivo a : archivos) {
                try {
                    Files.deleteIfExists(rootLocation.resolve(a.getNombreServidor()));
                } catch (Exception ignored) {}
                archivoRepo.deleteById(a.getId());
            }
            claseRepo.deleteById(claseId);
            return "redirect:/portafolio?tab=archivos&msg=del_ok";
        }
        return "redirect:/portafolio?tab=archivos";
    }

    @PostMapping("/contacto/enviar")
    public String enviarMensajeContacto(@RequestParam("mensaje") String mensaje, HttpSession session) {
        return "redirect:/portafolio?tab=contacto&msg=msg_enviado";
    }
}
