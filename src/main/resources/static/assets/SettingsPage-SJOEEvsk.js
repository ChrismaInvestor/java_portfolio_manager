import{h as E,k as F,l as N,m as G,C as _,s as M,n as V,o as U,_ as v,p as g,q as m,r as u,t as W,v as q,L as P,w as A,x as D,j as t,y as R,z,A as H,R as d,c as J,d as K,G as o,T as h,E as j,b as Q,B as X,g as y,F as Y,H as Z}from"./index-BRAUh-FV.js";import{L as ee,d as te}from"./Save-DaW34Pdi.js";const se=E("MuiBox",["root"]),ae=se,ne=F(),oe=N({themeId:G,defaultTheme:ne,defaultClassName:ae.root,generateClassName:_.generate}),re=oe,ie=["alignItems","autoFocus","component","children","dense","disableGutters","divider","focusVisibleClassName","selected","className"],ce=(e,s)=>{const{ownerState:a}=e;return[s.root,a.dense&&s.dense,a.alignItems==="flex-start"&&s.alignItemsFlexStart,a.divider&&s.divider,!a.disableGutters&&s.gutters]},le=e=>{const{alignItems:s,classes:a,dense:n,disabled:r,disableGutters:i,divider:c,selected:p}=e,l=z({root:["root",n&&"dense",!i&&"gutters",c&&"divider",r&&"disabled",s==="flex-start"&&"alignItemsFlexStart",p&&"selected"]},H,a);return v({},a,l)},de=M(V,{shouldForwardProp:e=>U(e)||e==="classes",name:"MuiListItemButton",slot:"Root",overridesResolver:ce})(({theme:e,ownerState:s})=>v({display:"flex",flexGrow:1,justifyContent:"flex-start",alignItems:"center",position:"relative",textDecoration:"none",minWidth:0,boxSizing:"border-box",textAlign:"left",paddingTop:8,paddingBottom:8,transition:e.transitions.create("background-color",{duration:e.transitions.duration.shortest}),"&:hover":{textDecoration:"none",backgroundColor:(e.vars||e).palette.action.hover,"@media (hover: none)":{backgroundColor:"transparent"}},[`&.${g.selected}`]:{backgroundColor:e.vars?`rgba(${e.vars.palette.primary.mainChannel} / ${e.vars.palette.action.selectedOpacity})`:m(e.palette.primary.main,e.palette.action.selectedOpacity),[`&.${g.focusVisible}`]:{backgroundColor:e.vars?`rgba(${e.vars.palette.primary.mainChannel} / calc(${e.vars.palette.action.selectedOpacity} + ${e.vars.palette.action.focusOpacity}))`:m(e.palette.primary.main,e.palette.action.selectedOpacity+e.palette.action.focusOpacity)}},[`&.${g.selected}:hover`]:{backgroundColor:e.vars?`rgba(${e.vars.palette.primary.mainChannel} / calc(${e.vars.palette.action.selectedOpacity} + ${e.vars.palette.action.hoverOpacity}))`:m(e.palette.primary.main,e.palette.action.selectedOpacity+e.palette.action.hoverOpacity),"@media (hover: none)":{backgroundColor:e.vars?`rgba(${e.vars.palette.primary.mainChannel} / ${e.vars.palette.action.selectedOpacity})`:m(e.palette.primary.main,e.palette.action.selectedOpacity)}},[`&.${g.focusVisible}`]:{backgroundColor:(e.vars||e).palette.action.focus},[`&.${g.disabled}`]:{opacity:(e.vars||e).palette.action.disabledOpacity}},s.divider&&{borderBottom:`1px solid ${(e.vars||e).palette.divider}`,backgroundClip:"padding-box"},s.alignItems==="flex-start"&&{alignItems:"flex-start"},!s.disableGutters&&{paddingLeft:16,paddingRight:16},s.dense&&{paddingTop:4,paddingBottom:4})),ue=u.forwardRef(function(s,a){const n=W({props:s,name:"MuiListItemButton"}),{alignItems:r="center",autoFocus:i=!1,component:c="div",children:p,dense:x=!1,disableGutters:l=!1,divider:S=!1,focusVisibleClassName:O,selected:k=!1,className:T}=n,f=q(n,ie),I=u.useContext(P),$=u.useMemo(()=>({dense:x||I.dense||!1,alignItems:r,disableGutters:l}),[r,I.dense,x,l]),b=u.useRef(null);A(()=>{i&&b.current&&b.current.focus()},[i]);const L=v({},n,{alignItems:r,dense:$.dense,disableGutters:l,divider:S,selected:k}),C=le(L),w=D(b,a);return t.jsx(P.Provider,{value:$,children:t.jsx(de,v({ref:w,href:f.href||f.to,component:(f.href||f.to)&&c==="div"?"button":c,focusVisibleClassName:R(C.focusVisible,O),ownerState:L,className:R(C.root,T)},f,{classes:C,children:p}))})}),pe=ue,B=u.createContext({});function xe(e,s){switch(s.type){case"countUpdate":return{...e,updateCount:e.updateCount+1};default:throw new Error}}function fe(){const e=d.useRef(null),{dispatch:s}=d.useContext(B),[a,n]=d.useState({name:"",description:"",account:""}),r=c=>{const{name:p,value:x}=c.target;n(l=>({...l,[p]:x}))},i=J({mutationFn:()=>fetch(X+"portfolio",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(a)})});return d.useMemo(()=>{s&&s({type:"countUpdate"})},[i.status==="success"]),t.jsx(K,{sx:{m:2,p:2},children:t.jsx("form",{ref:e,children:t.jsxs(o,{container:!0,spacing:2,children:[t.jsx(o,{item:!0,xs:12,children:t.jsx(h,{variant:"h5",children:"新增投资组合"})}),t.jsx(o,{item:!0,xs:12,children:t.jsx(j,{label:"名称",fullWidth:!0,placeholder:"请输入投资组合名称",name:"name",value:a.name,onChange:r,required:!0})}),t.jsx(o,{item:!0,xs:12,children:t.jsx(j,{label:"描述",fullWidth:!0,placeholder:"请描述该投资组合",name:"description",value:a.description,onChange:r,required:!0})}),t.jsx(o,{item:!0,xs:12,children:t.jsx(j,{label:"关联账户",fullWidth:!0,placeholder:"请输入在券商的关联账户",name:"account",value:a.account,onChange:r,required:!0})}),t.jsx(o,{item:!0,xs:12,children:i.isLoading?t.jsx(ee,{loading:i.isLoading,loadingPosition:"start",startIcon:t.jsx(te,{}),variant:"contained",disabled:!0,children:t.jsx("span",{children:"创建中"})}):t.jsx(Q,{variant:"contained",sx:{width:"25%"},onClick:()=>{var c;(c=e.current)!=null&&c.reportValidity()&&i.mutate()},children:"新增"})})]})})})}function ge(e){const{name:s,description:a,account:n}=e;return t.jsxs(o,{container:!0,children:[t.jsx(o,{item:!0,xs:6,children:t.jsxs(y,{spacing:1,children:[t.jsx(h,{children:t.jsx("strong",{children:s})}),t.jsx(h,{children:n})]})}),t.jsx(o,{item:!0,xs:6,children:t.jsx(y,{spacing:1,children:t.jsx(h,{variant:"body2",children:a})})})]})}function me(){const[e,s]=d.useState([]),{state:a}=d.useContext(B);return d.useEffect(()=>{Y().then(n=>{n&&s(n)})},[a.updateCount]),t.jsx(re,{sx:{p:2},children:t.jsx(y,{spacing:1,children:t.jsx(Z,{disablePadding:!0,children:e.map((n,r)=>t.jsx(pe,{sx:{border:1,borderColor:"#e5e7eb"},children:t.jsx(ge,{name:n.name,description:n.description,account:n.account})},r))})})})}const he={updateCount:0};function Ce(){const[e,s]=u.useReducer(xe,he);return t.jsx(B.Provider,{value:{state:e,dispatch:s},children:t.jsxs(o,{container:!0,spacing:2,children:[t.jsx(o,{item:!0,xs:6,children:t.jsx(me,{})}),t.jsx(o,{item:!0,xs:6,children:t.jsx(fe,{})})]})})}export{Ce as default};