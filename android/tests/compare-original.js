const fs=require('fs'),vm=require('vm');
const html=fs.readFileSync(process.argv[2],'utf8');
const rules=html.slice(html.indexOf(' const HOLIDAYS'),html.indexOf('</script>',html.indexOf(' const HOLIDAYS')));
const c={};vm.createContext(c);vm.runInContext(rules+`;this.status=statusEqA;this.rest=isRestDayCD;this.holidays=HOLIDAYS;this.pattern=PATTERN_AB_2;this.anchor=ANCHOR_223;`,c);
let lines=[];
// Covers pre-anchor dates, leap years, multiple 28/14/56 day cycles.
for(let y=2024;y<=2030;y++)for(let m=0;m<12;m++)for(let day=1;day<=31;day++){
 let d=new Date(y,m,day);if(d.getMonth()!==m)continue;
 const ds=d.toISOString().slice(0,10),delta=Math.floor((new Date(y,m,day,12)-c.anchor)/86400000);
 lines.push([ds,...['A','B','C','D'].map(t=>c.status(d,t)),c.rest(d),delta%56===0,c.holidays.has(ds)].join('|'));
}
fs.writeFileSync(process.argv[3],lines.join('\n'));
console.log(`Original JS: ${lines.length} days, ${lines.length*8} expected values generated`);
