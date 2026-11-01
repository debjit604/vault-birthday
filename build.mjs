import { mkdir, copyFile, cp, readFile, writeFile, rm } from 'node:fs/promises';
import { build } from 'esbuild';

const files = ['index.html', 'styles.css', 'app.js', 'manifest.json', 'service-worker.js'];
await rm('www', { recursive: true, force: true });
await mkdir('www', { recursive: true });
for (const file of files) await copyFile(file, `www/${file}`);
await cp('web/icons', 'www/icons', { recursive: true });
const html = await readFile('www/index.html', 'utf8');
await writeFile('www/index.html', html.replace('<script src="app.js" defer></script>', '<script src="native-notifications.bundle.js"></script><script src="app.js" defer></script>'));
await build({ entryPoints: ['native-notifications.js'], bundle: true, platform: 'browser', format: 'iife', outfile: 'www/native-notifications.bundle.js' });
console.log('Built Capacitor web assets in www/.');
