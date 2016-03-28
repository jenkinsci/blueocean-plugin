import React, { Component } from 'react';
import {ModalContainer, ModalDialog} from 'react-modal-dialog';

export default class Modale extends Component {

    constructor(props) {
        super(props);
        this.state = { isShowingModal: false };
    }

    render() {
        return (<div onClick={() => this.setState({isShowingModal: true})}>
            xxx
            {
                this.state.isShowingModal &&
                <ModalContainer onClose={() => this.setState({isShowingModal: false})}>
                    <ModalDialog onClose={() => this.setState({isShowingModal: false})}>
                        <h1>Dialog Content</h1>
                        <p>Lorem ipsum dolor sit amet, impetus dissentiunt vix ne. Vix accumsan adipisci no, ius no populo voluptaria, no eam viderer appareat persequeris. Ex harum tollit nullam mea. Mei sanctus placerat ut, ad mei recusabo instructior, quo eu nonumes deleniti principes. Ceteros oportere aliquando ei pro, et dolores forensibus quo, te zril adolescens vix. Pro at illum dicit referrentur, fabellas conclusionemque ne nam.
                        </p>
                        <p>
                            Mel nonumes salutandi cu, cu cum nobis altera. Nibh facilis fabellas quo ut, ex decore legere commodo est. Eum et errem ludus dolorem, ut eam offendit suavitate similique. No eirmod malorum eligendi pri. Nec an illum iracundia dissentias, ad solum justo facete vis, dicam aperiri fuisset vel et. Ne vim facilisis assueverit philosophia, an intellegebat consequuntur mei, soleat eruditi apeirian in duo.
                        </p>
                        <p>
                            No duo mazim labores sensibus, eum delenit indoctum instructior in. Vero primis vix te, ne sit eros porro repudiandae. Per platonem intellegam adversarium an. His et lorem animal labores, duo iudico ubique suscipit id.
                        </p>
                        <p>
                            Stet illum hendrerit ex eum. Dicant causae at pro, vis et vitae impedit assueverit, meis simul ad pro. Duis qualisque ad mel. Reque munere labitur et eum. Vidisse alterum quo in.
                        </p>
                        <p>
                            Pro odio voluptaria et, quo an disputationi comprehensam, quot possit apeirian cum id. Dico nominavi ad vim, tractatos abhorreant usu ea. Pri id eros eius, odio interpretaris ne duo. Et zril tamquam omittantur his, et quot eros vituperata nam. Omnes fuisset ut eum, harum clita cu usu.
                        </p>
                        <p>
                            Atqui efficiantur est in, nec vivendo abhorreant id. Modo vero vix in. Et sed hinc porro epicurei, amet pericula facilisis ius ne, et cum habemus placerat scribentur. In vix vidit iudico praesent, pri ad dicant equidem delenit. Agam utinam facilisis ius et, ei debet ceteros quo, ei putant audiam vix. No idque quodsi facilisis mea, reque malorum saperet ex sed, vis esse qualisque no.
                        </p>
                        <p>
                            Et sint duis persius mei, ei legere commodo fabulas usu. Eu postea admodum molestiae cum. At usu labores habemus phaedrum. Nibh prima te eos, vix at affert aliquip. Ex impedit partiendo similique nec.
                        </p>
                        <p>
                            Cum invidunt intellegebat an, mei te dico pertinax, sed at posse soleat nominati. Eum veri nonumes constituto ad. Ne illud integre vis, nam ancillae postulant ei. Saepe meliore docendi in eum, at vis assum adolescens, sea decore appellantur in. Ius et brute gubergren.
                        </p>
                        <p>
                            Vim diam feugait offendit no. Ne eos invenire senserit, mea eu homero pertinacia, elit reque quo in. Mel ut omnis maiorum, cu novum animal probatus usu. His an mutat minim latine, est eu nisl case. Fuisset copiosae vix in, ius te summo putent.
                        </p>
                        <p>
                            Eirmod impedit efficiendi id has. Cum in nostro gubergren. Blandit copiosae ex mea, an possim debitis menandri cum. Eos ut eius docendi, errem alienum sea at.
                        </p>
                        <p>
                            Sit novum laoreet id, cu mea facer quaeque voluptatum. Munere utroque ad eam. Stet putant posidonium ex vim, eum te putant referrentur, ea utroque constituam mei. Eripuit torquatos ius in, nihil referrentur mel ut. Nec tale quot blandit et. Vocibus ocurreret intellegebat mea in. Pro lucilius temporibus et.
                        </p>
                        <p>
                            Quo ea invidunt periculis corrumpit. Vim et postea nostrum euripidis, et dicam tantas consequuntur has. Velit sensibus ea nec. At partem oportere mediocritatem ius, eirmod minimum blandit ex his.
                        </p>
                        <p>
                            Quas sonet luptatum his te. Quo ea antiopam consequuntur. Mundi eloquentiam in sed, eam id error saperet ornatus. Audire conceptam nam in. Ei summo nullam phaedrum duo, vix id molestiae necessitatibus.
                        </p>
                        <p>
                            Sonet dolore dolores vim ut, per simul quodsi eu. Vis tota equidem inciderint ex. Et nihil vocibus torquatos ius, nostrud patrioque moderatius sed at. Essent copiosae ne vix, ei sententiae reprehendunt est. Fugit nobis laboramus ea quo, cum possim scripta ex. Ex per legendos complectitur.
                        </p>
                        <p>
                            Cum meis fabulas ei, ei sed vocibus convenire. Vim porro tractatos et, pri ea hinc populo. Ad ridens petentium democritum mel, sea ut mutat scripta tacimates. Scripserit interpretaris mea ei, in dicat utroque ius. No hendrerit inciderint sit, vix nostro dolorem inciderint ne. Copiosae antiopam cu nam. At sint congue quo.
                        </p>
                        <p>
                            Oporteat lobortis constituam mei eu, brute errem ad has. Minim praesent ut nam. Cibo debet singulis ne vis. Et zril tantas eum, aeque prodesset forensibus has te. Vel tation placerat ex, soleat imperdiet at pri. Qui ea nominavi repudiandae.
                        </p>
                        <p>
                            Et clita eruditi praesent mel, novum sensibus voluptatum sea et. Ut his eruditi definitionem. Nonumy prompta feugait an cum. Duo causae voluptatum ne, maiorum menandri adversarium sed an, periculis pertinacia sadipscing ne his.
                        </p>
                        <p>
                            Accusamus gubergren moderatius in eos, regione eripuit praesent ius ut, nonumy ceteros offendit sed ut. Per ex diceret invidunt consequat. Quod legimus eleifend et ius, an molestie voluptatum vim. His animal salutatus ea, ei his accusamus scripserit.
                        </p>
                        <p>
                            Per ex mentitum facilisi. Cu his sanctus eloquentiam ullamcorper, et brute voluptua vix, summo liber congue eum an. Solum prodesset deterruisset te has, an sea facer vidisse officiis, vix an illud neglegentur. Cu eum latine recteque consulatu.
                        </p>
                        <p>
                            Feugait delicata indoctum ut sed. Cu sea amet tempor officiis, case splendide liberavisse mei et, alii nominati similique ea sea. Id quo graece fastidii omnesque, no probo tation detracto pro. Sed ex aliquip percipit, ea vel munere deterruisset. Cu elitr quodsi detracto has.
                        </p>
                        <p>
                            Ea quaeque signiferumque nam. Affert ridens ex mei, tota facete per et, sit vitae meliore ancillae cu. Inani offendit disputationi no eum, vis blandit appellantur at, est cu tale vero. Ei atqui verear aliquip mei, case indoctum vix ne. Mei at legere ancillae pertinacia, est an fuisset conceptam, quo iriure saperet veritus ex. Novum dicant platonem eu cum, molestiae percipitur ne eam, no mel aliquid suscipit principes.
                        </p>
                        <p>
                            Sit et dolor aperiri, saepe soluta legimus cu qui. Mel constituto contentiones at. Usu vulputate voluptatibus ea. Vim volutpat concludaturque in. Alterum delicata ad vis. Nibh solum laudem eos ex, vel adhuc affert facilis ad, pri ad solum qualisque voluptatum.
                        </p>
                        <p>
                            Qui ne summo labitur lobortis, ut vel ceteros mandamus. Est prima populo verear at. Vivendo tibique maluisset ea quo, ad est iisque lucilius pertinacia. Magna lorem maiorum ei eum, eam dico urbanitas an, pri eius quidam percipit te. Ne vim oporteat partiendo.
                        </p>
                    </ModalDialog>
                </ModalContainer>

            }
        </div>);
    }
}
