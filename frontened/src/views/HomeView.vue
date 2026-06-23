<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const root = ref(null)
let observer = null

onMounted(() => {
  const targets = root.value?.querySelectorAll('.reveal') || []
  if (!('IntersectionObserver' in window)) { targets.forEach(t => t.classList.add('is-visible')); return }
  observer = new IntersectionObserver(
    entries => entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('is-visible'); observer.unobserve(e.target) } }),
    { threshold: 0.08, rootMargin: '0px 0px -30px 0px' },
  )
  targets.forEach(t => observer.observe(t))
})
onBeforeUnmount(() => { if (observer) observer.disconnect() })

const codeLines = [
  { t: '@Service' },
  { t: 'public class', v: 'UserService', e: ' {' },
  { t: '  @Autowired' },
  { t: '  private', v: 'UserRepository', e: ' repo;' },
  { t: '' },
  { t: '  public', v: 'User', f: 'findById', e: '(Long id) {' },
  { t: '    return', v: 'repo', e: '.findById(id)' },
  { t: '      .orElseThrow(() ->' },
  { t: '        new', v: 'NotFoundException', e: '(id));' },
  { t: '  }' },
  { t: '' },
  { t: '  public', v: 'List<User>', f: 'search', e: '(String q) {' },
  { t: '    return', v: 'repo', e: '.searchByKeyword(q);' },
  { t: '  }' },
  { t: '}' },
]

const typedLines = ref([])
let typeTimer = null
let typeIndex = 0

const startTyping = () => {
  if (typeTimer) return
  typeIndex = 0
  typedLines.value = []
  
  const typeNext = () => {
    if (typeIndex >= codeLines.length) {
      typeTimer = null
      return
    }
    typedLines.value.push({ ...codeLines[typeIndex] })
    typeIndex++
    const delay = codeLines[typeIndex - 1].t === '' ? 80 : 28 + Math.random() * 40
    typeTimer = setTimeout(typeNext, delay)
  }
  
  setTimeout(typeNext, 600)
}

onMounted(() => {
  const codeWin = root.value?.querySelector('.code-win')
  if (!codeWin) return
  const io = new IntersectionObserver(([e]) => {
    if (e.isIntersecting) {
      startTyping()
      io.disconnect()
    }
  }, { threshold: 0.3 })
  io.observe(codeWin)
})

onBeforeUnmount(() => {
  if (typeTimer) clearTimeout(typeTimer)
})

const steps = [
  { n:'01', icon:'M12 3v13.5M7 11l5 5.5 5-5.5M20 21H4', t:'上传项目', d:'Git 仓库克隆或 ZIP 上传，自动识别 Spring Boot 等主流项目结构' },
  { n:'02', icon:'M14.5 2h-9A1.5 1.5 0 0 0 4 3.5v17A1.5 1.5 0 0 0 5.5 22h13a1.5 1.5 0 0 0 1.5-1.5V7.5L14.5 2z M14 2v5.5a.5.5 0 0 0 .5.5H20 M8 13h8 M8 17h8 M8 9h2', t:'智能解析', d:'类与方法粒度切分，提取包名、注解、方法签名等元数据' },
  { n:'03', icon:'M9.663 17h4.673M12 3v1m-8 9h1m14 0h1M5.636 5.636l.707.707M4 12a8 8 0 1 0 16 0 8 8 0 0 0-16 0z', t:'LLM 摘要', d:'大模型为代码块生成中文自然语言描述，检索更精准' },
  { n:'04', icon:'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.3 7l8.7 5 8.7-5', t:'向量嵌入', d:'Embedding 模型 1536 维语义向量，毫秒级检索' },
]
const features = [
  { a:'#818cf8', i:'M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z M4 22v-7', t:'多语言解析', d:'Java、Python、TypeScript、Go 等主流语言一站式分析' },
  { a:'#22d3ee', i:'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M12 22V12', t:'向量检索', d:'pgvector 语义搜索，多维度过滤，毫秒级返回最相关代码块' },
  { a:'#f472b6', i:'M9.663 17h4.673 M4 12a8 8 0 1 0 16 0 8 8 0 0 0-16 0z', t:'AI 摘要', d:'LLM 为每个代码块生成中文描述，问答更有上下文' },
  { a:'#34d399', i:'M13 2L3 14h9l-1 8 10-12h-9l1-8z', t:'安全导入', d:'自动 Zip-Slip 防护、格式校验、文件大小限制' },
  { a:'#fbbf24', i:'M3 7v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2l-2-2H5a2 2 0 0 0-2 2z', t:'批量处理', d:'异步管道全自动解析→切分→增强→嵌入，支持大规模项目' },
  { a:'#a78bfa', i:'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z', t:'RAG 问答', d:'自然语言提问，语义检索 + LLM 上下文精准回答' },
]
const stats = [{ v:10000, s:'+', l:'代码块解析' },{ v:5, s:' 种', l:'语言支持' },{ v:1536, s:' 维', l:'向量嵌入' },{ v:99, s:'%', l:'检索精度' }]
const counters = ref(stats.map(()=>0))
let counted = false
function animC() {
  if (counted) return; counted = true
  stats.forEach((s,i)=>{ const step=s.v/(2000/16); let cur=0; function up(){ cur+=step; counters.value[i]=cur>=s.v?s.v:Math.floor(cur); if(cur<s.v) requestAnimationFrame(up) }; up() })
}
onMounted(()=>{
  const sec=root.value?.querySelector('.stats-band'); if(!sec) return; let d=false
  const so=new IntersectionObserver(([e])=>{ if(e.isIntersecting&&!d){d=true;animC();so.disconnect()} },{threshold:0.5}); so.observe(sec)
})
</script>

<template>
  <div ref="root" class="home">

    <!-- ═══════════════ HERO ═══════════════ -->
    <section class="hero">
      <div class="hero-grid">
        <div class="hero-left">
          <div class="hero-badge reveal"><span class="bd" />AI-Powered · RAG 代码智能检索</div>
          <h1 class="hero-h1 reveal">用 <em class="gt">AI</em> 读懂<br />每一行代码</h1>
          <p class="hero-p reveal" style="transition-delay:0.12s">上传项目 → 自动解析 → 语义索引 → 像对话一样探索代码库</p>
          <div class="hero-btns reveal" style="transition-delay:0.25s">
            <RouterLink to="/upload" class="btn-glow"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M12 3v13.5M7 11l5 5.5 5-5.5M20 21H4"/></svg>立即开始</RouterLink>
            <RouterLink to="/projects" class="btn-out"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>浏览项目</RouterLink>
          </div>
        </div>
        <div class="hero-right reveal" style="transition-delay:0.35s">
          <div class="code-win">
            <div class="cw-top"><span class="cw-d"/><span class="cw-d"/><span class="cw-d"/><span class="cw-title">UserService.java</span></div>
            <div class="cw-body">
              <div v-for="(line, i) in typedLines" :key="i" class="cl type-line">
                <span v-if="line.t" class="ck">{{ line.t }}</span>
                <span v-if="line.v" class="cn">{{ line.v }}</span>
                <span v-if="line.f" class="cf">{{ line.f }}</span>
                <span v-if="line.e">{{ line.e }}</span>
              </div>
              <span v-if="typedLines.length < codeLines.length" class="type-cursor">|</span>
            </div>
          </div>
        </div>
      </div>
      <div class="hero-scroll reveal" style="transition-delay:0.55s" @click="document.querySelector('.pipeline')?.scrollIntoView({behavior:'smooth'})"><span>向下探索</span><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg></div>
    </section>

    <!-- ═══════════════ PIPELINE ═══════════════ -->
    <section class="pipeline section">
      <div class="sec-head reveal"><span class="eyebrow">How It Works</span><h2>四步构建代码知识库</h2><p class="sub">从源码到智能问答，全自动化流水线</p></div>
      <div class="pipe-row">
        <template v-for="(s,i) in steps" :key="s.n">
          <div class="pipe-card reveal" :style="{ transitionDelay: `${i*0.1}s` }">
            <span class="pipe-num">{{ s.n }}</span>
            <div class="pipe-icon"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path :d="s.icon"/></svg></div>
            <h3>{{ s.t }}</h3>
            <p>{{ s.d }}</p>
          </div>
          <div v-if="i<steps.length-1" class="pipe-sep"><span class="ps-line"/><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="9 18 15 12 9 6"/></svg></div>
        </template>
      </div>
    </section>

    <!-- ═══════════════ FEATURES ═══════════════ -->
    <section class="features section">
      <div class="sec-head reveal"><span class="eyebrow">Features</span><h2>为开发者打造的智能引擎</h2></div>
      <div class="bento">
        <div v-for="(f,i) in features" :key="f.t" class="bento-card reveal" :style="{ transitionDelay: `${i*0.07}s`, '--accent': f.a }">
          <div class="bc-icon" :style="{background:f.a}"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path :d="f.i"/></svg></div>
          <h3>{{ f.t }}</h3>
          <p>{{ f.d }}</p>
        </div>
      </div>
    </section>

    <!-- ═══════════════ STATS ═══════════════ -->
    <section class="stats-band">
      <div class="stats-row">
        <div v-for="(s,i) in stats" :key="s.l" class="stat reveal" :style="{ transitionDelay: `${i*0.08}s` }">
          <span class="sv">{{ counters[i] }}<small>{{ s.s }}</small></span>
          <span class="sl">{{ s.l }}</span>
        </div>
      </div>
    </section>

    <!-- ═══════════════ CTA ═══════════════ -->
    <section class="cta section">
      <div class="cta-card reveal">
        <h2>准备好探索代码了吗？</h2>
        <p>拖入一个项目，让 AI 为你解读每一行代码的含义。</p>
        <div class="cta-row">
          <RouterLink to="/upload" class="btn-glow"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 3v13.5M7 11l5 5.5 5-5.5M20 21H4"/></svg>上传项目</RouterLink>
          <RouterLink to="/projects" class="btn-out">查看项目 <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg></RouterLink>
        </div>
      </div>
    </section>

    <footer class="hf">
      <span class="fw">Code Analyzer</span>
      <span class="fm">Vue 3 + Ant Design</span>
      <nav><a href="https://github.com/Elopez4ever/code-analyzer" target="_blank">GitHub</a><RouterLink to="/upload">上传</RouterLink><RouterLink to="/projects">项目</RouterLink></nav>
    </footer>
  </div>
</template>

<style scoped>
.home { position: relative; }
.section { padding: 90px 32px; position: relative; z-index: 1; }
.sec-head { text-align: center; margin-bottom: 56px; }
.sec-head h2 { font-size: clamp(26px,4vw,40px); font-weight: 750; color: var(--text); margin: 6px 0 10px; letter-spacing: -0.025em; }
[data-theme='dark'] .sec-head h2 { color: #fff; }
.eyebrow { text-transform: uppercase; letter-spacing: 2.5px; font-size: 11px; font-weight: 700; color: var(--primary); }
.sub { color: var(--muted); font-size: 15px; margin: 0; }
.reveal { opacity: 0; transform: translateY(28px); transition: opacity 0.7s cubic-bezier(0.16,1,0.3,1), transform 0.7s cubic-bezier(0.16,1,0.3,1); }
.reveal.is-visible { opacity: 1; transform: translateY(0); }

/* Buttons */
.btn-glow { display: inline-flex; align-items: center; gap: 10px; padding: 14px 32px; border-radius: 14px; border: none; background: var(--primary-gradient); color: #fff; font-size: 15px; font-weight: 650; cursor: pointer; box-shadow: 0 4px 24px rgba(99,102,241,0.35); text-decoration: none; position: relative; overflow: hidden; transition: transform 0.2s ease, box-shadow 0.3s ease; }
.btn-glow::after { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.18), transparent); animation: sh 3s ease-in-out infinite; pointer-events: none; }
@keyframes sh { 0%{left:-100%} 100%{left:100%} }
.btn-glow:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(99,102,241,0.5); }
[data-theme='dark'] .btn-glow { box-shadow: 0 4px 28px rgba(99,102,241,0.45), 0 0 60px rgba(99,102,241,0.1); }
.btn-out { display: inline-flex; align-items: center; gap: 8px; padding: 14px 28px; border-radius: 14px; background: var(--surface); border: 1px solid var(--border); color: var(--text); font-size: 15px; font-weight: 600; cursor: pointer; text-decoration: none; backdrop-filter: blur(12px); transition: all 0.25s ease; }
.btn-out:hover { border-color: rgba(99,102,241,0.4); transform: translateY(-2px); }
[data-theme='dark'] .btn-out { background: rgba(255,255,255,0.04); border-color: rgba(255,255,255,0.12); color: rgba(255,255,255,0.85); }
[data-theme='dark'] .btn-out:hover { border-color: rgba(139,92,246,0.5); background: rgba(255,255,255,0.08); }

/* Hero */
.hero { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 110px 32px 80px; position: relative; z-index: 1; overflow: hidden; }
.hero-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 56px; align-items: center; max-width: 1120px; width: 100%; }
.hero-left { max-width: 500px; }
.hero-badge { display: inline-flex; align-items: center; gap: 8px; margin-bottom: 24px; padding: 5px 18px; border-radius: 999px; background: rgba(99,102,241,0.08); border: 1px solid rgba(99,102,241,0.18); color: var(--primary); font-size: 13px; font-weight: 500; backdrop-filter: blur(12px); }
.bd { width: 6px; height: 6px; border-radius: 50%; background: var(--primary); animation: bp 2.2s ease-in-out infinite; }
@keyframes bp { 0%,100%{opacity:0.5;transform:scale(1)} 50%{opacity:1;transform:scale(1.5)} }
.hero-h1 { font-size: clamp(38px,6vw,68px); font-weight: 880; line-height: 1.06; letter-spacing: -0.04em; margin: 0; color: var(--text); }
[data-theme='dark'] .hero-h1 { color: #fff; text-shadow: 0 0 80px rgba(129,140,248,0.12); }
.gt { font-style: normal; background: linear-gradient(135deg, #6366f1, #8b5cf6, #0891b2); background-size: 200% 100%; -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; animation: gs 4s ease-in-out infinite; }
[data-theme='dark'] .gt { background: linear-gradient(135deg, #818cf8, #c084fc, #22d3ee, #f472b6); background-size: 300% 100%; -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; }
@keyframes gs { 0%,100%{background-position:0% 50%} 50%{background-position:100% 50%} }
.hero-p { color: var(--muted); font-size: clamp(14px,1.8vw,17px); line-height: 1.7; margin: 20px 0 32px; }
[data-theme='dark'] .hero-p { color: rgba(255,255,255,0.45); }
.hero-btns { display: flex; gap: 14px; flex-wrap: wrap; }

.hero-right { display: flex; justify-content: center; }
.code-win { background: #16132a; border-radius: 16px; overflow: hidden; width: 100%; max-width: 480px; box-shadow: 0 24px 64px rgba(0,0,0,0.35), 0 0 50px rgba(99,102,241,0.08); border: 1px solid rgba(255,255,255,0.08); }
[data-theme='light'] .code-win { background: #1a1832; box-shadow: 0 16px 48px rgba(0,0,0,0.15); }
.cw-top { display: flex; align-items: center; gap: 8px; padding: 12px 18px; background: rgba(0,0,0,0.3); }
.cw-d { width: 10px; height: 10px; border-radius: 50%; }
.cw-d:nth-child(1){background:#ff5f57}.cw-d:nth-child(2){background:#febc2e}.cw-d:nth-child(3){background:#28c840}
.cw-title { font-size: 12px; color: rgba(255,255,255,0.4); font-family: 'Fira Code',monospace; margin-left: 8px; }
.cw-body { padding: 18px 22px; font-family: 'Fira Code','Cascadia Code',monospace; font-size: 12.5px; line-height: 1.95; color: rgba(255,255,255,0.5); min-height: 280px; }
.cl { white-space: pre; }
.ck { color: #c084fc; } .cn { color: #7dd3fc; } .cv { color: #fbbf24; } .cf { color: #f472b6; }
.type-line { animation: typeIn 0.15s ease-out; }
@keyframes typeIn { from { opacity: 0; transform: translateY(2px); } to { opacity: 1; transform: translateY(0); } }
.type-cursor { display: inline-block; color: #7dd3fc; font-weight: 300; animation: blink 1s ease-in-out infinite; }
@keyframes blink { 0%,50%{opacity:1} 51%,100%{opacity:0} }

.hero-scroll { display: flex; flex-direction: column; align-items: center; gap: 6px; color: var(--muted); font-size: 12px; cursor: pointer; opacity: 0.45; transition: opacity 0.3s; position: absolute; bottom: 28px; left: 50%; transform: translateX(-50%); }
.hero-scroll:hover { opacity: 1; }
.hero-scroll svg { animation: bnc 2s ease-in-out infinite; }
@keyframes bnc { 0%,100%{transform:translateY(0)} 50%{transform:translateY(6px)} }

/* Pipeline */
.pipeline { background: linear-gradient(160deg, #15103a 0%, #1a1040 40%, #0f1b3a 100%); border-top: 1px solid rgba(129,140,248,0.12); border-bottom: 1px solid rgba(139,92,246,0.1); }
[data-theme='light'] .pipeline { background: linear-gradient(160deg, #eef0ff 0%, #f0eafa 40%, #e8f4ff 100%); border-color: rgba(99,102,241,0.12); }
.pipe-row { display: flex; justify-content: center; align-items: flex-start; gap: 0; flex-wrap: wrap; max-width: 1040px; margin: 0 auto; }
.pipe-card { flex: 0 0 220px; text-align: center; padding: 28px 16px; border-radius: 20px; background: rgba(99,102,241,0.04); border: 1px solid rgba(99,102,241,0.08); transition: box-shadow 0.3s, transform 0.3s, border-color 0.3s, background 0.3s; }
.pipe-card:hover { transform: translateY(-4px); background: rgba(99,102,241,0.08); border-color: rgba(99,102,241,0.2); }
[data-theme='light'] .pipe-card { background: rgba(255,255,255,0.7); border-color: rgba(99,102,241,0.1); }
[data-theme='light'] .pipe-card:hover { background: #fff; box-shadow: 0 8px 28px rgba(99,102,241,0.1); }
.pipe-num { font-family: 'Fira Code',monospace; font-size: 48px; font-weight: 850; background: var(--primary-gradient); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; opacity: 0.85; line-height: 1; display: block; margin-bottom: 16px; }
.pipe-icon { width: 54px; height: 54px; border-radius: 15px; background: rgba(99,102,241,0.08); display: grid; place-items: center; margin: 0 auto 14px; color: var(--primary); transition: all 0.3s; }
.pipe-card:hover .pipe-icon { background: var(--primary-gradient); color: #fff; transform: scale(1.05); }
.pipe-card h3 { font-size: 16px; font-weight: 700; margin: 0 0 6px; color: var(--text); }
[data-theme='dark'] .pipe-card h3 { color: #fff; }
.pipe-card p { font-size: 13px; line-height: 1.55; margin: 0; color: var(--muted); }
.pipe-sep { display: flex; align-items: center; margin-top: 36px; padding: 0 4px; flex-shrink: 0; }
.ps-line { width: 36px; height: 2px; border-radius: 2px; background: linear-gradient(90deg, rgba(99,102,241,0.5), rgba(139,92,246,0.7)); background-size: 200% 100%; animation: fl 2s linear infinite; }
@keyframes fl { 0%{background-position:0% 50%} 100%{background-position:200% 50%} }
.pipe-sep svg { color: rgba(139,92,246,0.5); animation: ap 1.6s ease-in-out infinite; }
@keyframes ap { 0%,100%{opacity:0.3;transform:translateX(0)} 50%{opacity:1;transform:translateX(4px)} }

/* Features */
.features { background: transparent; position: relative; }
.bento { display: grid; grid-template-columns: repeat(3,1fr); gap: 18px; max-width: 1060px; margin: 0 auto; }
.bento-card { background: var(--surface); border: 1px solid var(--border); border-radius: 20px; padding: 26px 24px; position: relative; overflow: hidden; box-shadow: var(--shadow-sm); transition: transform 0.25s ease, box-shadow 0.35s ease, border-color 0.35s ease; }
.bento-card::after { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, transparent, var(--accent, #818cf8), transparent); opacity: 0; transition: opacity 0.3s; }
.bento-card:hover { transform: translateY(-2px); border-color: rgba(99,102,241,0.3); box-shadow: var(--shadow-lg), 0 0 20px rgba(99,102,241,0.05); }
.bento-card:hover::after { opacity: 0.6; }
[data-theme='dark'] .bento-card { background: rgba(255,255,255,0.025); border-color: rgba(255,255,255,0.06); box-shadow: none; }
[data-theme='dark'] .bento-card:hover { background: rgba(255,255,255,0.05); border-color: rgba(255,255,255,0.15); box-shadow: 0 16px 40px rgba(0,0,0,0.3); }
.bc-icon { width: 40px; height: 40px; border-radius: 11px; display: grid; place-items: center; margin-bottom: 14px; flex-shrink: 0; }
.bento-card h3 { font-size: 16px; font-weight: 700; margin: 0 0 6px; color: var(--text); }
[data-theme='dark'] .bento-card h3 { color: #fff; }
.bento-card p { font-size: 13px; line-height: 1.6; margin: 0; color: var(--muted); }

/* Stats */
.stats-band { padding: 80px 32px; position: relative; z-index: 1; background: linear-gradient(135deg, #1a0a30 0%, #0d1b3a 50%, #0a1a30 100%); border-top: 1px solid rgba(168,85,247,0.2); border-bottom: 1px solid rgba(34,211,238,0.15); }
[data-theme='light'] .stats-band { background: linear-gradient(135deg, #ede4ff 0%, #e0f0ff 50%, #e4f4ff 100%); border-color: rgba(99,102,241,0.2); }
.stats-row { display: flex; justify-content: center; gap: 72px; max-width: 760px; margin: 0 auto; flex-wrap: wrap; }
.stat { text-align: center; }
.sv { font-family: 'Fira Code',monospace; font-size: 48px; font-weight: 850; color: var(--text); line-height: 1; }
[data-theme='dark'] .sv { color: #fff; text-shadow: 0 0 40px rgba(129,140,248,0.3); }
.sv small { font-size: 18px; font-weight: 650; color: var(--primary); }
.sl { display: block; font-size: 12px; color: var(--muted); font-weight: 500; margin-top: 6px; text-transform: uppercase; letter-spacing: 1.5px; }

/* CTA */
.cta { background: linear-gradient(160deg, #13103a 0%, #1a1040 50%, #101a38 100%); border-top: 1px solid rgba(129,140,248,0.15); }
[data-theme='light'] .cta { background: linear-gradient(160deg, #eef0ff 0%, #f2e8ff 50%, #e8f2ff 100%); border-color: rgba(99,102,241,0.12); }
.cta-card { text-align: center; max-width: 580px; margin: 0 auto; padding: 64px 48px; border-radius: 26px; background: linear-gradient(135deg, rgba(99,102,241,0.2), rgba(139,92,246,0.15), rgba(34,211,238,0.08)); border: 1px solid rgba(139,92,246,0.3); box-shadow: 0 0 80px rgba(99,102,241,0.12); backdrop-filter: blur(20px); }
[data-theme='light'] .cta-card { background: linear-gradient(135deg, rgba(99,102,241,0.12), rgba(139,92,246,0.08)); border-color: rgba(99,102,241,0.25); box-shadow: 0 0 50px rgba(99,102,241,0.06); }
.cta-card h2 { font-size: clamp(22px,3.5vw,34px); font-weight: 750; margin: 0 0 10px; color: var(--text); }
[data-theme='dark'] .cta-card h2 { color: #fff; }
.cta-card p { color: var(--muted); font-size: 15px; margin: 0 0 28px; }
.cta-row { display: flex; gap: 14px; justify-content: center; flex-wrap: wrap; }

/* Footer */
.hf { display: flex; align-items: center; justify-content: center; gap: 24px; padding: 32px; flex-wrap: wrap; z-index: 1; position: relative; background: #0b0818; border-top: 1px solid rgba(168,85,247,0.15); color: var(--muted); font-size: 13px; }
[data-theme='light'] .hf { background: #e8e4f8; border-color: rgba(99,102,241,0.1); }
.hf nav { display: flex; gap: 18px; }
.hf a { color: var(--muted); text-decoration: none; transition: color 0.2s; }
.hf a:hover { color: var(--primary); }
.fw { font-weight: 700; color: var(--text); }
.fm { color: var(--muted); }

@media (max-width: 860px) {
  .hero-grid { grid-template-columns: 1fr; text-align: center; }
  .hero-left { max-width: 100%; }
  .hero-btns { justify-content: center; }
  .hero-right { display: none; }
  .bento { grid-template-columns: 1fr 1fr; }
  .pipe-row { flex-direction: column; align-items: center; gap: 8px; }
  .pipe-sep { transform: rotate(90deg); margin-top: 0; }
  .stats-row { gap: 40px; }
}
@media (max-width: 480px) {
  .hero-h1 { font-size: 30px; }
  .section { padding: 60px 20px; }
  .bento { grid-template-columns: 1fr; }
  .cta-card { padding: 40px 24px; }
  .sv { font-size: 38px; }
}
</style>
