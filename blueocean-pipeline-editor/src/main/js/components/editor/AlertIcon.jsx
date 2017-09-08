import React from 'react';

export default () => (
    <svg className="alerticon" width="20px" height="20px" viewBox="13 9 20 20">
        <defs>
            <path d="M8.0197096,1.74273849 C8.56110904,0.780250597 9.44018119,0.782544345 9.9802904,1.74273849 L17.0197096,14.2572615 C17.561109,15.2197494 17.1073772,16 16.0049107,16 L1.99508929,16 C0.893231902,16 0.440181194,15.2174557 0.980290398,14.2572615 L8.0197096,1.74273849 Z" id="path-1"></path>
            <mask id="mask-2" maskContentUnits="userSpaceOnUse" maskUnits="objectBoundingBox" x="0" y="0" width="20" height="20">
                <rect x="0" y="0" width="20" height="20" fill="white"></rect>
                <use xlinkHref="#path-1" fill="black"></use>
            </mask>
            <rect id="path-3" x="8" y="6" width="2" height="4"></rect>
            <mask id="mask-4" maskContentUnits="userSpaceOnUse" maskUnits="objectBoundingBox" x="0" y="0" width="2" height="4" fill="white">
                <use xlinkHref="#path-3"></use>
            </mask>
            <rect id="path-5" x="8" y="12" width="2" height="2"></rect>
            <mask id="mask-6" maskContentUnits="userSpaceOnUse" maskUnits="objectBoundingBox" x="0" y="0" width="2" height="2" fill="white">
                <use xlinkHref="#path-5"></use>
            </mask>
        </defs>
        <g id="Group-10" stroke="none" stroke-width="1" fill="none" fill-rule="evenodd" transform="translate(15, 9)">
            <g id="Triangle-2">
                <use fill="#CE373A" fill-rule="evenodd" xlinkHref="#path-1"></use>
                <use stroke="#FFFFFF" mask="url(#mask-2)" stroke-width="2" xlinkHref="#path-1"></use>
            </g>
            <use id="Rectangle-17" stroke="#FFFFFF" mask="url(#mask-4)" stroke-width="2" fill="#D8D8D8" xlinkHref="#path-3"></use>
            <use id="Rectangle-17-Copy" stroke="#FFFFFF" mask="url(#mask-6)" stroke-width="2" fill="#D8D8D8" xlinkHref="#path-5"></use>
        </g>
    </svg>
);
