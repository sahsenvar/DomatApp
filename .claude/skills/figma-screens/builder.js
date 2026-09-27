// DomatApp Figma ekran kurucu — use_figma çağrısının başına olduğu gibi yapıştır, sonra ekranları tanımla.
// Akış C'de (17 durum) sınanmış hâli. Her helper bir öğrenim kaydını kodla önler (bkz. ai/design/learnings.yaml).
//
// Kullanım (aynı çağrının devamında):
//   out.push(await screen('C9@default', 0, 9000, { header: 'Başlık', body: [ ... ], bar: [ ... ] }));
//   return JSON.stringify(out);
//
// Konum kuralı: her akış bir satır (y), her durum 450 px arayla (x). Mevcut satırları görmek için önce
// sayfadaki çerçeveleri listele; üst üste bindirme.

for (const st of ['Regular','Medium','SemiBold','Bold','ExtraBold']) { try { await figma.loadFontAsync({family:'Nunito Sans', style: st}); } catch(e){} }

// Sayfa ve kütüphane: tüm sayfalardaki bileşen setleri + tekil bileşenler (ikonlar, ScreenHeader).
let page; const L = {};
const SCREENS_PAGE = 'Screens · Akış C';   // akışa göre değiştir; yoksa oluştur
for (const p of figma.root.children) { await figma.setCurrentPageAsync(p);
  for (const s of p.findAll(n => n.type === 'COMPONENT_SET' || (n.type==='COMPONENT' && n.parent.type!=='COMPONENT_SET'))) L[s.name] = s;
  if (p.name.startsWith(SCREENS_PAGE)) page = p; }
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

// L-010: resize() auto-layout boyutlandırmasını FIXED yapar. Genişliği sabitleyip yüksekliği içeriğe bırakmak için bunu kullan.
function fixWidth(f, w){ f.resize(w, Math.max(f.height, 1)); if (f.layoutMode==='VERTICAL') { f.counterAxisSizingMode='FIXED'; f.primaryAxisSizingMode='AUTO'; } else if (f.layoutMode==='HORIZONTAL') { f.primaryAxisSizingMode='FIXED'; f.counterAxisSizingMode='AUTO'; } return f; }

// Kütüphane örneği. variant: {state:'empty'} ; props: bileşen property'leri (ad öneki yeter: 'label' → 'label#6:0');
// overrides: iç içe örnek property'leri {'trailingAction.text': 'Düzenle'}.
function inst(setName, layer, variant, props, overrides){
  const s = L[setName]; if (!s) throw new Error('no comp '+setName);
  let c = s;
  if (s.type==='COMPONENT_SET') { c = s.children.find(ch => Object.entries(variant||{}).every(([k,v]) => ch.variantProperties[k]===String(v))); if (!c) throw new Error(setName+' no variant '+JSON.stringify(variant)); }
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

// Çocukları ekle: varsayılan FILL genişlik; meta {hug:true} ise HUG. FormSection meta {title, kids}.
async function fill(parent, kids){ for (const k of kids){ const m = M.get(k)||{}; parent.appendChild(k); k.layoutSizingHorizontal = m.hug ? 'HUG' : 'FILL'; if (k.type==='TEXT') k.textAutoResize='HEIGHT';
  if (m.kids){ const t = await T(m.title,'title-large','color/on-surface','title'); k.appendChild(t); t.layoutSizingHorizontal='FILL'; t.textAutoResize='HEIGHT'; await fill(k, m.kids); } } }

// FormSection: Figma'da çerçeve (bileşen değil). Katman adı 'FormSection · <bölge-id>'.
const FS = (id, title, kids) => meta(F('FormSection · '+id,'VERTICAL',{pad:['sp4','sp4','sp4','sp4'], gap:'sp3', fill:'color/surface', radius:'md', stroke:'color/outline-variant'}), {title, kids});
// Ekrana özgü düz metin: katman adı 'text · <bölge-id>'. Metin kartın strings'inden gelmeli.
const text = async (id, s, style, color) => T(s, style, color, 'text · '+id);

// Ekran: ScreenHeader (opsiyonel) + body (sp4/sp4) + BottomActionBar (opsiyonel).
// o.hug=true → içerik yüksekliği (kaydırılan ekran); aksi hâlde 844 sabit, body kalan alanı doldurur.
async function screen(name, x, y, o){
  const old = page.findOne(n => n.type==='FRAME' && n.name===name && n.parent===page); if (old) old.remove();
  const root = F(name,'VERTICAL',{fill:'color/background'}); page.appendChild(root); root.resize(390, 844); root.counterAxisSizingMode='FIXED';
  root.primaryAxisSizingMode = o.hug ? 'AUTO' : 'FIXED'; root.x=x; root.y=y; root.clipsContent = true;
  if (o.header){ const h = inst('Bar/ScreenHeader','ScreenHeader · header',null,{title:o.header, showBack: o.showBack !== false}); root.appendChild(h); h.layoutSizingHorizontal='FILL'; }
  const body = F('body','VERTICAL',{pad:['sp4','sp4','sp4','sp4'], gap:'sp4', align:o.bodyAlign}); root.appendChild(body); body.layoutSizingHorizontal='FILL'; body.layoutSizingVertical = o.hug ? 'HUG' : 'FILL';
  await fill(body, o.body);
  if (o.bar){ const bar = F('BottomActionBar · '+(o.barId||'cta'),'VERTICAL',{pad:['sp4','sp4','sp4','sp4'], gap:'sp3', fill:'color/surface'}); bar.strokes=[paint('color/outline-variant')]; bar.strokeTopWeight=1; bar.strokeBottomWeight=0; bar.strokeLeftWeight=0; bar.strokeRightWeight=0; bar.strokeAlign='INSIDE';
    root.appendChild(bar); bar.layoutSizingHorizontal='FILL'; await fill(bar, o.bar); }
  return [name, root.id, Math.round(root.height)]; }

const out = [];
// ---- ekran tanımları buradan sonra ----
