import { mkdir, copyFile, cp, readFile, writeFile, rm } from 'node:fs/promises';
import { execSync } from 'node:child_process';
import { build } from 'esbuild';

const files = ['index.html', 'styles.css', 'app.js', 'manifest.json', 'service-worker.js'];
const sha = (process.env.GITHUB_SHA || (() => {
  try { return execSync('git rev-parse HEAD').toString().trim(); }
  catch { return 'local'; }
})()).slice(0, 7);
const stamp = `build #${process.env.GITHUB_RUN_NUMBER || 'dev'} · ${sha}`;

await rm('www', { recursive: true, force: true });
await mkdir('www', { recursive: true });
for (const file of files) await copyFile(file, `www/${file}`);
await cp('web/icons', 'www/icons', { recursive: true });

if (process.env.KEEP_SW !== '1') {
  await writeFile('www/service-worker.js', `self.addEventListener('install',()=>self.skipWaiting());
self.addEventListener('activate',e=>e.waitUntil((async()=>{for(const k of await caches.keys())await caches.delete(k);await self.registration.unregister();})()));`);
}

let html = await readFile('www/index.html', 'utf8');
const scriptPattern = /<script[^>]*\ssrc=["']app\.js["'][^>]*><\/script>/;
if (!scriptPattern.test(html)) throw new Error('index.html: app.js <script> tag not found, cannot inject native bundle');
html = html.replace(scriptPattern, match => `<script src="native-notifications.bundle.js"></script>${match}`);
html = html.replace('</body>', `<div id="build-stamp" style="text-align:center;font:12px monospace;opacity:.5;padding:16px">${stamp}</div></body>`);
await writeFile('www/index.html', html);

await build({
  entryPoints: ['native-notifications.js'],
  bundle: true,
  platform: 'browser',
  format: 'iife',
  outfile: 'www/native-notifications.bundle.js'
});
console.log('Built www/ —', stamp);
