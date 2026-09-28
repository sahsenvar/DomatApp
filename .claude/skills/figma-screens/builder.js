// DomatApp Figma ekran kurucu v2 — use_figma çağrısının başına olduğu gibi yapıştır, sonra ekranları tanımla.
// v1: Akış C (17 durum). v2: Akış B ile — tek prototip sayfası, akış bölümleri, iç içe kaydırma, prototip helper'ları.
//
// Kullanım (aynı çağrının devamında):
//   const S = await section('Akış B');                       // sayfadaki akış bölümü (yoksa açılır)
//   out.push(await screen(S, 'B1@default', 0, 0, { top:[...], body:[...], bar:[...], bottom:[...], overlay:[[node,x,y]] }));
//   return JSON.stringify(out);
//
// Yerleşim: her ekran bir satır (y), her durum 450 px arayla (x) — koordinatlar BÖLÜM içindedir.
// Prototip bağlantıları ayrı bir "wiring" çağrısında kurulur (hedef çerçevelerin hepsi var olmalı): link/back/setVar.

for (const st of ['Regular','Medium','SemiBold','Bold','ExtraBold']) { try { await figma.loadFontAsync({family:'Nunito Sans', style: st}); } catch(e){} }

// Kütüphane: tüm sayfalardaki bileşen setleri + tekil bileşenler. Ekranlar TEK sayfada ('Screens'): prototip
// bağlantıları sayfalar arasında çalışmaz (L-032).
let page; const L = {};
const SCREENS_PAGE = 'Screens';
for (const p of figma.root.children) { await figma.setCurrentPageAsync(p);
  for (const s of p.findAll(n => n.type === 'COMPONENT_SET' || (n.type==='COMPONENT' && n.parent.type!=='COMPONENT_SET'))) L[s.name] = s;
  if (p.name === SCREENS_PAGE) page = p; }
if (!page) { page = figma.createPage(); page.name = SCREENS_PAGE; }
await figma.setCurrentPageAsync(page);

// L-014: Figma düğümlerine özel alan eklenemez → meta veriyi Map'te tut.
const M = new Map(); const meta = (n, o) => { M.set(n, Object.assign(M.get(n)||{}, o)); return n; };

const vars = await figma.variables.getLocalVariablesAsync();
const V = n => { const v = vars.find(v => v.name === n); if (!v) throw new Error('no var '+n); return v; };
// Renk her zaman değişkene bağlı. Opaklık GEREKİYORSA düğüm opaklığı kullan (L-011), dolgu opaklığı değil.
const paint = name => figma.variables.setBoundVariableForPaint({type:'SOLID', color:{r:0,g:0,b:0}, opacity:1}, 'color', V(name));
const styles = await figma.getLocalTextStylesAsync();
const TS = n => { const s = styles.find(s => s.name === n); if (!s) throw new Error('no style '+n); return s; };

async function T(chars, style, color, name){ const t = figma.createText(); await t.setTextStyleIdAsync(TS(style).id); t.characters = chars; t.fills=[paint(color)]; if(name) t.name=name; return t; }

// Auto-layout çerçeve. Boşluk/köşe/renk yalnızca token değişkenleriyle.
function F(name, mode, o={}){ const f = figma.createFrame(); f.name=name; f.fills=[]; if(mode){ f.layoutMode=mode; f.primaryAxisSizingMode='AUTO'; f.counterAxisSizingMode='AUTO'; }
  if (o.gap) f.setBoundVariable('itemSpacing', V('spacing/'+o.gap));
  if (o.pad){ const [t,r,b,l]=o.pad; [['paddingTop',t],['paddingRight',r],['paddingBottom',b],['paddingLeft',l]].forEach(([k,v])=>{ if(v) f.setBoundVariable(k, V('spacing/'+v)); }); }
  if (o.fill) f.fills=[paint(o.fill)];
  if (o.radius) for (const k of ['topLeftRadius','topRightRadius','bottomLeftRadius','bottomRightRadius']) f.setBoundVariable(k, V('radius/'+o.radius));
  if (o.stroke){ f.strokes=[paint(o.stroke)]; f.strokeWeight=o.sw||1; f.strokeAlign='INSIDE'; }
  if (o.align) f.primaryAxisAlignItems=o.align; if (o.cross) f.counterAxisAlignItems=o.cross;
  return f; }

// L-010: resize() auto-layout boyutlandırmasını FIXED yapar.
function fixWidth(f, w){ f.resize(w, Math.max(f.height, 1)); if (f.layoutMode==='VERTICAL') { f.counterAxisSizingMode='FIXED'; f.primaryAxisSizingMode='AUTO'; } else if (f.layoutMode==='HORIZONTAL') { f.primaryAxisSizingMode='FIXED'; f.counterAxisSizingMode='AUTO'; } return f; }

// Kütüphane örneği. variant: {state:'empty'} ; props: property ad öneki ('label' → 'label#6:0');
// overrides: iç içe örnek property'leri {'trailingAction.text': 'Düzenle'}.
function inst(setName, layer, variant, props, overrides){
  const s = L[setName]; if (!s) throw new Error('no comp '+setName);
  let c = s;
  if (s.type==='COMPONENT_SET') { c = s.children.find(ch => Object.entries(variant||{}).every(([k,v]) => ch.variantProperties[k]===String(v))); if (!c) throw new Error(setName+' no variant '+JSON.stringify(variant)+' in '+s.children.map(x=>x.name).join('|')); }
  const i = c.createInstance(); i.name = layer;
  const defs = i.componentProperties; const P={};
  for (const [k,v] of Object.entries(props||{})) { const key = Object.keys(defs).find(x => x===k || x.startsWith(k+'#')); if (!key) throw new Error(setName+' no prop '+k+' in '+Object.keys(defs)); P[key]=v; }
  if (Object.keys(P).length) i.setProperties(P);
  for (const [path, val] of Object.entries(overrides||{})) {
    const [instName, prop] = path.split('.');
    const sub = i.findOne(n => n.type==='INSTANCE' && n.name===instName); if (!sub) throw new Error(setName+' no sub '+instName);
    const k = Object.keys(sub.componentProperties).find(x => x===prop || x.startsWith(prop+'#')); if (!k) throw new Error('no subprop '+path);
    sub.setProperties({[k]: val});
  }
  return i; }
const ICONC = n => { const c = L['Icon/'+n]; if (!c) throw new Error('no icon '+n); return c; };
// Örneğin içindeki 'icon' katmanını başka Icon/* ile değiştir (StatusHero boş sepet, ağ hatası).
function swapIcon(i, name, color){ const ic=i.findOne(n=>n.type==='INSTANCE'&&n.name==='icon'); ic.swapComponent(ICONC(name)); if(color){ const v=ic.findOne(k=>k.type==='VECTOR'); v.fills=[paint(color)]; } return i; }
const setText = (i, prop, val) => i.setProperties({[Object.keys(i.componentProperties).find(k=>k===prop||k.startsWith(prop+'#'))]: val});

// Çocukları ekle: varsayılan FILL genişlik; meta {hug:true} ise HUG. FormSection meta {title, kids}.
async function fill(parent, kids){ for (const k of kids){ const m = M.get(k)||{}; parent.appendChild(k); k.layoutSizingHorizontal = m.hug ? 'HUG' : 'FILL'; if (k.type==='TEXT') k.textAutoResize='HEIGHT';
  if (m.kids){ const t = await T(m.title,'title-large','color/on-surface','title'); k.appendChild(t); t.layoutSizingHorizontal='FILL'; t.textAutoResize='HEIGHT'; await fill(k, m.kids); } } }

const FS = (id, title, kids) => meta(F('FormSection · '+id,'VERTICAL',{pad:['sp4','sp4','sp4','sp4'], gap:'sp3', fill:'color/surface', radius:'md', stroke:'color/outline-variant'}), {title, kids});
const text = async (id, s, style, color) => T(s, style, color, 'text · '+id);
const note = (id, t, tone) => { const tones = L['Feedback/InlineNote'].children.map(x=>x.variantProperties.tone); return inst('Feedback/InlineNote','InlineNote · '+id,{tone: tones.find(x=>new RegExp(tone||'eutral','i').test(x))},{text:t}); };
function banner(layer, tone, body, title, actions){ const b=inst('Feedback/InfoBanner',layer,{tone},{body, showTitle:!!title, showActions:!!(actions&&actions.length)}); if(title) setText(b,'title',title);
  if (actions){ b.findAll(n=>n.type==='INSTANCE'&&n.mainComponent&&n.mainComponent.parent&&n.mainComponent.parent.name==='Button/TextLink').forEach((l,i)=>{ if(i<actions.length) setText(l,'text',actions[i]); else l.visible=false; }); }
  return b; }

// Akış bölümü: sayfada SECTION. Ekran koordinatları bölüm içindedir.
async function section(name, y){ let s = page.findOne(n=>n.type==='SECTION'&&n.name===name);
  if (!s){ s = figma.createSection(); s.name=name; page.appendChild(s); s.x=0; s.y = y!=null ? y : Math.max(0,...page.children.filter(n=>n!==s).map(n=>n.y+n.height+400)); s.resizeWithoutConstraints(3000, 1000); }
  return s; }
function grow(S){ const r = Math.max(...S.children.map(n=>n.x+n.width))+200, b = Math.max(...S.children.map(n=>n.y+n.height))+200; S.resizeWithoutConstraints(Math.max(S.width,r), Math.max(S.height,b)); }

// Ekran: 390×844 sabit. header (ScreenHeader) + top (kaymayan, tam genişlik) + body (İÇ İÇE KAYDIRMA: bleed + content)
// + bar (BottomActionBar, sabit) + bottom (BottomNav, sabit) + overlay (mutlak konum: FAB, snackbar).
// body.overflowDirection=VERTICAL → prototipte içerik kayar, bar/nav sabit kalır (L-033). Statik görselde taşan kısım kesilir;
// annotations.md'de "kayar" diye yazılır. o.bleed: kenar boşluksuz, içerikle birlikte kayan düğümler (ürün görseli).
async function screen(S, name, x, y, o){
  const old = S.findOne(n => n.type==='FRAME' && n.name===name && n.parent===S); if (old) old.remove();
  const root = F(name,'VERTICAL',{fill:'color/background'}); S.appendChild(root); root.resize(390, 844); root.counterAxisSizingMode='FIXED'; root.primaryAxisSizingMode='FIXED';
  root.x=x; root.y=y; root.clipsContent = true;
  if (o.header != null){ const h = inst('Bar/ScreenHeader','ScreenHeader · header',null,{title:o.header, showBack: o.showBack !== false}); root.appendChild(h); h.layoutSizingHorizontal='FILL'; }
  for (const t of (o.top||[])) { root.appendChild(t); t.layoutSizingHorizontal='FILL'; }
  const body = F('body','VERTICAL',{}); root.appendChild(body); body.layoutSizingHorizontal='FILL'; body.layoutSizingVertical='FILL'; body.clipsContent=true; body.overflowDirection='VERTICAL';
  for (const b of (o.bleed||[])) { body.appendChild(b); b.layoutSizingHorizontal='FILL'; }
  const content = F('content','VERTICAL',{pad:['sp4','sp4','sp4','sp4'], gap:'sp4', align:o.bodyAlign}); body.appendChild(content); content.layoutSizingHorizontal='FILL';
  if (o.bodyAlign) { content.layoutSizingVertical='FILL'; content.counterAxisAlignItems='CENTER'; }
  await fill(content, o.body||[]);
  if (o.bar){ const bar = F('BottomActionBar · '+(o.barId||'cta'),'VERTICAL',{pad:['sp4','sp4','sp4','sp4'], gap:'sp3', fill:'color/surface'}); bar.strokes=[paint('color/outline-variant')]; bar.strokeTopWeight=1; bar.strokeBottomWeight=0; bar.strokeLeftWeight=0; bar.strokeRightWeight=0; bar.strokeAlign='INSIDE';
    root.appendChild(bar); bar.layoutSizingHorizontal='FILL'; await fill(bar, o.bar); }
  for (const b of (o.bottom||[])) { root.appendChild(b); b.layoutSizingHorizontal='FILL'; }
  for (const [n,ox,oy] of (o.overlay||[])) { root.appendChild(n); n.layoutPositioning='ABSOLUTE'; n.x=ox; n.y=oy; }
  if (o.sheet) await modal(root, 'sheet', o.sheet);
  if (o.dialog) await modal(root, 'dialog', o.dialog);
  grow(S);
  return [name, root.id]; }

// Popup'lar akış içinde çizilir (kural 6): scrim (color/scrim, düğüm opaklığı 0,32) + BottomSheet ya da Dialog çerçevesi.
// o.sheet  = { title, kids:[...] }                      → alttan, üst köşeler xl, tutamak
// o.dialog = { title, body, kids:[...], actions:[{text, kind:'ghost'|'link'|'primary'|'danger'}] } → ortada, 342 genişlik
async function modal(root, kind, m){
  const sc = F('scrim', null, {fill:'color/scrim'}); sc.opacity = 0.32; root.appendChild(sc); sc.layoutPositioning='ABSOLUTE'; sc.resize(390,844); sc.x=0; sc.y=0;
  if (kind==='sheet'){
    const sh = F('BottomSheet · '+(m.id||'sheet'),'VERTICAL',{pad:['sp2','sp4','sp6','sp4'], gap:'sp3', fill:'color/surface', cross:'CENTER'});
    for (const k of ['topLeftRadius','topRightRadius']) sh.setBoundVariable(k, V('radius/xl'));
    root.appendChild(sh); sh.layoutPositioning='ABSOLUTE'; fixWidth(sh, 390);
    const h = F('handle',null,{fill:'color/outline-variant'}); h.resize(32,4); h.cornerRadius=2; sh.appendChild(h);
    if (m.title){ const t = await T(m.title,'headline-small','color/on-surface','title'); sh.appendChild(t); t.layoutSizingHorizontal='FILL'; }
    await fill(sh, m.kids||[]); sh.x=0; sh.y=844-sh.height; return sh; }
  const dg = F('Dialog · '+(m.id||'dialog'),'VERTICAL',{pad:['sp6','sp6','sp6','sp6'], gap:'sp4', fill:'color/surface', radius:'xl'});
  root.appendChild(dg); dg.layoutPositioning='ABSOLUTE'; fixWidth(dg, 342);
  const t = await T(m.title,'headline-small','color/on-surface','title'); dg.appendChild(t); t.layoutSizingHorizontal='FILL'; t.textAutoResize='HEIGHT';
  if (m.body){ const b = await T(m.body,'body-medium','color/on-surface-variant','body'); dg.appendChild(b); b.layoutSizingHorizontal='FILL'; b.textAutoResize='HEIGHT'; }
  await fill(dg, m.kids||[]);
  if (m.actions){ const row = F('actions','HORIZONTAL',{gap:'sp2', align:'MAX', cross:'CENTER'}); dg.appendChild(row); row.layoutSizingHorizontal='FILL';
    for (const a of m.actions){ const set = a.kind==='primary'?'Button/Primary': a.kind==='danger'?'Button/Destructive': a.kind==='link'?'Button/TextLink':'Button/Ghost';
      const v = a.kind==='primary' ? {size:'Small',enabled:'true',loading:'false'} : a.kind==='danger' ? {enabled:'true'} : (L[set].type==='COMPONENT_SET' ? Object.fromEntries(Object.entries(L[set].defaultVariant.variantProperties)) : null);
      // Diyalog satırında düğmeler içerik genişliğinde (L-035): Ghost/TextLink/Destructive varsayılan genişlikleri satırı taşırır.
      const b = inst(set, 'action · '+a.text, v, {text:a.text}); row.appendChild(b); b.layoutSizingHorizontal='HUG';
      if (a.kind==='danger') { b.setBoundVariable('paddingLeft', V('spacing/sp4')); b.setBoundVariable('paddingRight', V('spacing/sp4')); } } }
  dg.x = 24; dg.y = Math.round((844-dg.height)/2); return dg; }

// ---------------- Prototip (ayrı "wiring" çağrısında) ----------------
const frameByName = n => { const f = page.findOne(x => x.type==='FRAME' && x.name===n && x.parent && x.parent.type==='SECTION'); if (!f) throw new Error('no frame '+n); return f; };
// Çerçeve içinde katman: 'ScreenHeader · header' ya da 'ScreenHeader · header > back' (iç içe ad yolu).
function layer(frame, path){ let n = frame; for (const part of path.split(' > ')) { n = n.findOne(x => x.name===part); if (!n) throw new Error(frame.name+': no layer '+path); } return n; }
const PUSH = {type:'MOVE_IN', direction:'LEFT', matchLayers:false, easing:{type:'EASE_OUT'}, duration:0.25};
const FADE = {type:'DISSOLVE', easing:{type:'EASE_OUT'}, duration:0.2};
const SMART = {type:'SMART_ANIMATE', easing:{type:'EASE_OUT'}, duration:0.25};
const go = (target, tr) => ({type:'NODE', destinationId: frameByName(target).id, navigation:'NAVIGATE', transition: tr===undefined ? PUSH : tr, preserveScrollPosition:false});
const backA = () => ({type:'BACK'});
// Prototip değişkenleri: koleksiyon 'Prototype' (tasarım token'larından ayrı).
const pvars = vars.filter(v => v.name.startsWith('proto/'));
const PV = n => { const v = pvars.find(v=>v.name==='proto/'+n); if (!v) throw new Error('no proto var '+n); return v; };
const alias = v => ({type:'VARIABLE_ALIAS', resolvedType:v.resolvedType, value:{type:'VARIABLE_ALIAS', id:v.id}});
const setVar = (n, value) => ({type:'SET_VARIABLE', variableId: PV(n).id, variableValue: typeof value==='object' ? value : {type: typeof value==='boolean'?'BOOLEAN':typeof value==='number'?'FLOAT':'STRING', resolvedType: typeof value==='boolean'?'BOOLEAN':typeof value==='number'?'FLOAT':'STRING', value}});
const add = (n, d) => ({type:'EXPRESSION', resolvedType:'FLOAT', value:{expressionFunction:'ADDITION', expressionArguments:[alias(PV(n)), {type:'FLOAT', resolvedType:'FLOAT', value:d}]}});
const eq = (n, val) => ({type:'EXPRESSION', resolvedType:'BOOLEAN', value:{expressionFunction:'EQUALS', expressionArguments:[alias(PV(n)), {type:typeof val==='number'?'FLOAT':'BOOLEAN', resolvedType:typeof val==='number'?'FLOAT':'BOOLEAN', value:val}]}});
const when = (cond, thenA, elseA) => ({type:'CONDITIONAL', conditionalBlocks:[{condition:cond, actions:thenA}].concat(elseA ? [{actions:elseA}] : [])});
// Sayaç: FLOAT değişken + ekranda gösterilen STRING ikizi (metin yalnızca STRING değişkene bağlanır — L-034).
function counter(n, d, min, max){ const acts=[when({type:'EXPRESSION',resolvedType:'BOOLEAN',value:{expressionFunction: d>0?'LESS_THAN':'GREATER_THAN', expressionArguments:[alias(PV(n)),{type:'FLOAT',resolvedType:'FLOAT',value:d>0?max:min}]}}, [setVar(n, add(n,d))])];
  const blocks=[]; for (let v=min; v<=max; v++) blocks.push({condition:eq(n,v), actions:[setVar(n+'Text', String(v))]}); acts.push({type:'CONDITIONAL', conditionalBlocks:blocks}); return acts; }
async function on(node, actions, trigger){ await node.setReactionsAsync([{trigger: trigger||{type:'ON_CLICK'}, actions}]); return node.id; }
// Örnek içindeki metni STRING prototip değişkenine bağla (ör. sepet rozeti, miktar).
function bindText(node, n){ const t = node.type==='TEXT' ? node : node.findOne(x=>x.type==='TEXT' && (x.name==='value'||x.name==='cartCount'||x.name==='text')); t.setBoundVariable('characters', PV(n)); return t.id; }

const out = [];
// ---- ekran tanımları buradan sonra ----
