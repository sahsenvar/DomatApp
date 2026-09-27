// DomatApp tasarım paketi dışa aktarıcı — structure.json'ın "states" içeriğini üretir.
// use_figma'ya yapıştır; ONLY ve MAN'ı ayarla. Çıktı 20 KB ile sınırlı (L-013): betik 19,5 KB'ı aşarsa
// 'TOO_LONG' döner → ONLY'yi daralt (ekran başına bir çağrı).
//
// MAN = design/components.yaml'dan Figma adı → manifest anahtarı. Güncel listeyi üretmek için:
//   python3 -c "import yaml,json;d=yaml.safe_load(open('design/components.yaml'))['components'];print(json.dumps({v['figma']['name']:k for k,v in d.items() if isinstance(v.get('figma'),dict) and v['figma'].get('name')},ensure_ascii=False))"

const ONLY = /^(C1)@/;          // dışa aktarılacak ekran(lar)
const SCREENS_PAGE = 'Screens · Akış C';
const MAN = { /* yukarıdaki komutun çıktısı */ };

let page; for (const p of figma.root.children) if (p.name.startsWith(SCREENS_PAGE)) page = p;
await figma.setCurrentPageAsync(page);
const styles = await figma.getLocalTextStylesAsync(); const SN = id => (styles.find(s=>s.id===id)||{}).name;
// L-020: yalnızca gerçekten görünen metinler (visible zinciri). Gizlemek için opacity değil visible=false kullan.
const vis = n => { for (let p = n; p && p.type!=='PAGE'; p = p.parent) if (!p.visible) return false; return true; };
async function node(n){
  if (n.type==='INSTANCE') {
    const mc = await n.getMainComponentAsync(); const setName = mc.parent.type==='COMPONENT_SET' ? mc.parent.name : mc.name;
    const props = {}; for (const [k,v] of Object.entries(n.componentProperties)) { if (v.type==='INSTANCE_SWAP') continue; props[k.split('#')[0]] = v.value; }
    return {layer:n.name, component:setName, manifest: MAN[setName]||null, props,
            texts:[...new Set(n.findAll(t=>t.type==='TEXT' && vis(t) && t.characters.trim()).map(t=>t.characters))]};
  }
  if (n.type==='TEXT') return {layer:n.name, text:n.characters, style:SN(n.textStyleId)};
  if (n.type==='FRAME') { const o = {layer:n.name}; if (n.layoutMode && n.layoutMode!=='NONE') o.layout = n.layoutMode; o.children = []; for (const c of n.children) if (c.visible) o.children.push(await node(c)); return o; }
  return {layer:n.name, type:n.type};
}
const out = {};
for (const f of page.children.filter(n => n.type==='FRAME' && ONLY.test(n.name))) {
  const [screen, state] = f.name.split('@');
  (out[screen] = out[screen] || {})[state] = {figmaNodeId: f.id, size:[f.width, f.height], tree: await node(f)};
}
const s = JSON.stringify(out); return s.length > 19500 ? 'TOO_LONG '+s.length : s;
