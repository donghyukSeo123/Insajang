/* eslint-disable */
// @mui material components
import Grid from "@mui/material/Grid";

// Material Dashboard 2 React components
import MDBox from "components/MDBox";
import MDButton from "components/MDButton";
/* src/layouts/dashboard/index.js 등에 추가 */
// Material Dashboard 2 React example components
import DashboardLayout from "examples/LayoutContainers/DashboardLayout";
import DashboardNavbar from "examples/Navbars/DashboardNavbar";
import Footer from "examples/Footer";
import ComplexStatisticsCard from "examples/Cards/StatisticsCards/ComplexStatisticsCard";

// Dashboard components
import Projects from "layouts/dashboard/components/Projects";
import OrdersOverview from "layouts/dashboard/components/OrdersOverview";

import axios from "axios";

function Dashboard() {

    const handleSync = async () => {
        try {
            const userId = 1; // 테스트용 유저 ID (DB에 있는 ID)
            const response = await axios.get(`http://localhost:8080/api/instagram/sync/${userId}`);
            alert(response.data); // "동기화 성공!" 메시지 출력
        } catch (error) {
            console.error(error);
            alert("동기화 중 오류 발생");
        }
    };

    return (
        <DashboardLayout>
            <DashboardNavbar />
            <MDBox py={3}>
                {/* 상단 4구역: 인사장 실시간 현황 */}
                <Grid container spacing={3}>
                    <Grid item xs={12} md={6} lg={3}>
                        <MDBox mb={1.5}>
                            <ComplexStatisticsCard
                                color="dark"
                                icon="visibility"
                                title="오늘 방문자"
                                count={128}
                                percentage={{
                                    color: "success",
                                    amount: "+55%",
                                    label: "어제 대비",
                                }}
                            />
                        </MDBox>
                    </Grid>
                    <Grid item xs={12} md={6} lg={3}>
                        <MDBox mb={1.5}>
                            <ComplexStatisticsCard
                                icon="message"
                                title="새 방명록"
                                count="34"
                                percentage={{
                                    color: "success",
                                    amount: "+3",
                                    label: "최근 1시간",
                                }}
                            />
                        </MDBox>
                    </Grid>
                    <Grid item xs={12} md={6} lg={3}>
                        <MDBox mb={1.5}>
                            <ComplexStatisticsCard
                                color="success"
                                icon="share"
                                title="카톡 공유"
                                count="89"
                                percentage={{
                                    color: "success",
                                    amount: "+12%",
                                    label: "누적 공유수",
                                }}
                            />
                        </MDBox>
                    </Grid>
                    <Grid item xs={12} md={6} lg={3}>
                        <MDBox mb={1.5}>
                            <ComplexStatisticsCard
                                color="primary"
                                icon="favorite"
                                title="축하 하트"
                                count="+2,300"
                                percentage={{
                                    color: "success",
                                    amount: "",
                                    label: "실시간 합계",
                                }}
                            />
                        </MDBox>
                    </Grid>
                </Grid>
                <MDButton variant="gradient" color="info" onClick={handleSync}>
                    인스타 데이터 동기화
                </MDButton>
                {/* 하단 섹션: 실제 데이터 관리 */}
                <MDBox mt={4.5}>
                    <Grid container spacing={3}>
                        <Grid item xs={12} md={6} lg={8}>
                            {/* 최근 방명록 리스트 */}
                            <Projects title="최근 방명록 내역" />
                        </Grid>
                        <Grid item xs={12} md={6} lg={4}>
                            {/* 시스템 알림 및 활동 로그 */}
                            <OrdersOverview title="최근 활동 알림" />
                        </Grid>
                    </Grid>
                </MDBox>
            </MDBox>
            <Footer />
        </DashboardLayout>
    );
}

export default Dashboard;