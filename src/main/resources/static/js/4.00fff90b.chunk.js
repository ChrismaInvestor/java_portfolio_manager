"use strict";(self.webpackChunkreact_portfolio_manager=self.webpackChunkreact_portfolio_manager||[]).push([[4],{7535:function(e,t,n){n.d(t,{v:function(){return o}});var a=n(4165),r=n(5861),i=n(6435),o=function(){var e=(0,r.Z)((0,a.Z)().mark((function e(){var t,n;return(0,a.Z)().wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,fetch(i._+"portfolio",{method:"GET",headers:{"Content-Type":"application/json"}});case 2:if(200!==(t=e.sent).status){e.next=8;break}return e.next=6,t.json();case 6:return n=e.sent,e.abrupt("return",n);case 8:case"end":return e.stop()}}),e)})));return function(){return e.apply(this,arguments)}}()},5004:function(e,t,n){n.r(t),n.d(t,{default:function(){return Y}});var a=n(9439),r=n(1889),i=n(2791),o=n(4942),s=n(1413),c=n(5527),d=n(890),l=n(7391),u=n(6151),p=n(6435),m=n(3418),v=n(9709),f=n(3329),h=(0,i.createContext)({});function x(e,t){if("countUpdate"===t.type)return(0,s.Z)((0,s.Z)({},e),{},{updateCount:e.updateCount+1});throw new Error}var Z=n(184);function g(){var e=i.useRef(null),t=i.useContext(h).dispatch,n=i.useState({name:"",description:"",account:""}),x=(0,a.Z)(n,2),g=x[0],b=x[1],j=function(e){var t=e.target,n=t.name,a=t.value;b((function(e){return(0,s.Z)((0,s.Z)({},e),{},(0,o.Z)({},n,a))}))},C=(0,m.D)({mutationFn:function(){return fetch(p._+"portfolio",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(g)})}});return i.useMemo((function(){t&&t({type:"countUpdate"})}),["success"===C.status]),(0,Z.jsx)(c.Z,{sx:{m:2,p:2},children:(0,Z.jsx)("form",{ref:e,children:(0,Z.jsxs)(r.ZP,{container:!0,spacing:2,children:[(0,Z.jsx)(r.ZP,{item:!0,xs:12,children:(0,Z.jsx)(d.Z,{variant:"h5",children:"\u65b0\u589e\u6295\u8d44\u7ec4\u5408"})}),(0,Z.jsx)(r.ZP,{item:!0,xs:12,children:(0,Z.jsx)(l.Z,{label:"\u540d\u79f0",fullWidth:!0,placeholder:"\u8bf7\u8f93\u5165\u6295\u8d44\u7ec4\u5408\u540d\u79f0",name:"name",value:g.name,onChange:j,required:!0})}),(0,Z.jsx)(r.ZP,{item:!0,xs:12,children:(0,Z.jsx)(l.Z,{label:"\u63cf\u8ff0",fullWidth:!0,placeholder:"\u8bf7\u63cf\u8ff0\u8be5\u6295\u8d44\u7ec4\u5408",name:"description",value:g.description,onChange:j,required:!0})}),(0,Z.jsx)(r.ZP,{item:!0,xs:12,children:(0,Z.jsx)(l.Z,{label:"\u5173\u8054\u8d26\u6237",fullWidth:!0,placeholder:"\u8bf7\u8f93\u5165\u5728\u5238\u5546\u7684\u5173\u8054\u8d26\u6237",name:"account",value:g.account,onChange:j,required:!0})}),(0,Z.jsx)(r.ZP,{item:!0,xs:12,children:C.isLoading?(0,Z.jsx)(v.Z,{loading:C.isLoading,loadingPosition:"start",startIcon:(0,Z.jsx)(f.Z,{}),variant:"contained",disabled:!0,children:(0,Z.jsx)("span",{children:"\u521b\u5efa\u4e2d"})}):(0,Z.jsx)(u.Z,{variant:"contained",sx:{width:"25%"},onClick:function(){var t;null!==(t=e.current)&&void 0!==t&&t.reportValidity()&&C.mutate()},children:"\u65b0\u589e"})})]})})})}var b=n(6314);function j(e){var t=e.name,n=e.description,a=e.account;return(0,Z.jsxs)(r.ZP,{container:!0,children:[(0,Z.jsx)(r.ZP,{item:!0,xs:6,children:(0,Z.jsxs)(b.Z,{spacing:1,children:[(0,Z.jsx)(d.Z,{children:(0,Z.jsx)("strong",{children:t})}),(0,Z.jsx)(d.Z,{children:a})]})}),(0,Z.jsx)(r.ZP,{item:!0,xs:6,children:(0,Z.jsx)(b.Z,{spacing:1,children:(0,Z.jsx)(d.Z,{variant:"body2",children:n})})})]})}var C=n(7462),y=n(3366),P=n(3733),k=n(2421),I=n(104),O=n(8519),w=n(418),N=["className","component"];var F=n(5902),S=n(7107),G=n(988),R=(0,S.Z)(),T=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{},t=e.themeId,n=e.defaultTheme,a=e.defaultClassName,r=void 0===a?"MuiBox-root":a,o=e.generateClassName,s=(0,k.ZP)("div",{shouldForwardProp:function(e){return"theme"!==e&&"sx"!==e&&"as"!==e}})(I.Z);return i.forwardRef((function(e,a){var i=(0,w.Z)(n),c=(0,O.Z)(e),d=c.className,l=c.component,u=void 0===l?"div":l,p=(0,y.Z)(c,N);return(0,Z.jsx)(s,(0,C.Z)({as:u,ref:a,className:(0,P.Z)(d,o?o(r):r),theme:t&&i[t]||i},p))}))}({themeId:G.Z,defaultTheme:R,defaultClassName:"MuiBox-root",generateClassName:F.Z.generate}),q=T,B=n(493),V=n(4419),M=n(2065),_=n(6934),L=n(1402),W=n(7479),D=n(162),E=n(2071),U=n(6199),z=n(4065),A=["alignItems","autoFocus","component","children","dense","disableGutters","divider","focusVisibleClassName","selected","className"],J=(0,_.ZP)(W.Z,{shouldForwardProp:function(e){return(0,_.FO)(e)||"classes"===e},name:"MuiListItemButton",slot:"Root",overridesResolver:function(e,t){var n=e.ownerState;return[t.root,n.dense&&t.dense,"flex-start"===n.alignItems&&t.alignItemsFlexStart,n.divider&&t.divider,!n.disableGutters&&t.gutters]}})((function(e){var t,n=e.theme,a=e.ownerState;return(0,C.Z)((t={display:"flex",flexGrow:1,justifyContent:"flex-start",alignItems:"center",position:"relative",textDecoration:"none",minWidth:0,boxSizing:"border-box",textAlign:"left",paddingTop:8,paddingBottom:8,transition:n.transitions.create("background-color",{duration:n.transitions.duration.shortest}),"&:hover":{textDecoration:"none",backgroundColor:(n.vars||n).palette.action.hover,"@media (hover: none)":{backgroundColor:"transparent"}}},(0,o.Z)(t,"&.".concat(z.Z.selected),(0,o.Z)({backgroundColor:n.vars?"rgba(".concat(n.vars.palette.primary.mainChannel," / ").concat(n.vars.palette.action.selectedOpacity,")"):(0,M.Fq)(n.palette.primary.main,n.palette.action.selectedOpacity)},"&.".concat(z.Z.focusVisible),{backgroundColor:n.vars?"rgba(".concat(n.vars.palette.primary.mainChannel," / calc(").concat(n.vars.palette.action.selectedOpacity," + ").concat(n.vars.palette.action.focusOpacity,"))"):(0,M.Fq)(n.palette.primary.main,n.palette.action.selectedOpacity+n.palette.action.focusOpacity)})),(0,o.Z)(t,"&.".concat(z.Z.selected,":hover"),{backgroundColor:n.vars?"rgba(".concat(n.vars.palette.primary.mainChannel," / calc(").concat(n.vars.palette.action.selectedOpacity," + ").concat(n.vars.palette.action.hoverOpacity,"))"):(0,M.Fq)(n.palette.primary.main,n.palette.action.selectedOpacity+n.palette.action.hoverOpacity),"@media (hover: none)":{backgroundColor:n.vars?"rgba(".concat(n.vars.palette.primary.mainChannel," / ").concat(n.vars.palette.action.selectedOpacity,")"):(0,M.Fq)(n.palette.primary.main,n.palette.action.selectedOpacity)}}),(0,o.Z)(t,"&.".concat(z.Z.focusVisible),{backgroundColor:(n.vars||n).palette.action.focus}),(0,o.Z)(t,"&.".concat(z.Z.disabled),{opacity:(n.vars||n).palette.action.disabledOpacity}),t),a.divider&&{borderBottom:"1px solid ".concat((n.vars||n).palette.divider),backgroundClip:"padding-box"},"flex-start"===a.alignItems&&{alignItems:"flex-start"},!a.disableGutters&&{paddingLeft:16,paddingRight:16},a.dense&&{paddingTop:4,paddingBottom:4})})),H=i.forwardRef((function(e,t){var n=(0,L.Z)({props:e,name:"MuiListItemButton"}),a=n.alignItems,r=void 0===a?"center":a,o=n.autoFocus,s=void 0!==o&&o,c=n.component,d=void 0===c?"div":c,l=n.children,u=n.dense,p=void 0!==u&&u,m=n.disableGutters,v=void 0!==m&&m,f=n.divider,h=void 0!==f&&f,x=n.focusVisibleClassName,g=n.selected,b=void 0!==g&&g,j=n.className,k=(0,y.Z)(n,A),I=i.useContext(U.Z),O=i.useMemo((function(){return{dense:p||I.dense||!1,alignItems:r,disableGutters:v}}),[r,I.dense,p,v]),w=i.useRef(null);(0,D.Z)((function(){s&&w.current&&w.current.focus()}),[s]);var N=(0,C.Z)({},n,{alignItems:r,dense:O.dense,disableGutters:v,divider:h,selected:b}),F=function(e){var t=e.alignItems,n=e.classes,a=e.dense,r=e.disabled,i={root:["root",a&&"dense",!e.disableGutters&&"gutters",e.divider&&"divider",r&&"disabled","flex-start"===t&&"alignItemsFlexStart",e.selected&&"selected"]},o=(0,V.Z)(i,z.t,n);return(0,C.Z)({},n,o)}(N),S=(0,E.Z)(w,t);return(0,Z.jsx)(U.Z.Provider,{value:O,children:(0,Z.jsx)(J,(0,C.Z)({ref:S,href:k.href||k.to,component:(k.href||k.to)&&"div"===d?"button":d,focusVisibleClassName:(0,P.Z)(F.focusVisible,x),ownerState:N,className:(0,P.Z)(F.root,j)},k,{classes:F,children:l}))})})),K=n(7535);function Q(){var e=i.useState([]),t=(0,a.Z)(e,2),n=t[0],r=t[1],o=i.useContext(h).state;return i.useEffect((function(){(0,K.v)().then((function(e){e&&r(e)}))}),[o.updateCount]),(0,Z.jsx)(q,{sx:{p:2},children:(0,Z.jsx)(b.Z,{spacing:1,children:(0,Z.jsx)(B.Z,{disablePadding:!0,children:n.map((function(e,t){return(0,Z.jsx)(H,{sx:{border:1,borderColor:"#e5e7eb"},children:(0,Z.jsx)(j,{name:e.name,description:e.description,account:e.account})},t)}))})})})}var X={updateCount:0};function Y(){var e=(0,i.useReducer)(x,X),t=(0,a.Z)(e,2),n=t[0],o=t[1];return(0,Z.jsx)(h.Provider,{value:{state:n,dispatch:o},children:(0,Z.jsxs)(r.ZP,{container:!0,spacing:2,children:[(0,Z.jsx)(r.ZP,{item:!0,xs:6,children:(0,Z.jsx)(Q,{})}),(0,Z.jsx)(r.ZP,{item:!0,xs:6,children:(0,Z.jsx)(g,{})})]})})}}}]);
//# sourceMappingURL=4.00fff90b.chunk.js.map