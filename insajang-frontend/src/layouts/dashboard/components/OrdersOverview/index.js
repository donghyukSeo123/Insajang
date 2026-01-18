// @mui material components
import Card from "@mui/material/Card";
import Icon from "@mui/material/Icon";

// Material Dashboard 2 React components
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";

// Material Dashboard 2 React example components
import TimelineItem from "examples/Timeline/TimelineItem";

function LiveFeedback() {
  return (
    <Card sx={{ height: "100%" }}>
      <MDBox pt={3} px={3}>
        <MDTypography variant="h6" fontWeight="medium">
          실시간 반응 현황
        </MDTypography>
        <MDBox mt={0} mb={2}>
          <MDTypography variant="button" color="text" fontWeight="regular">
            <MDTypography display="inline" variant="body2" verticalAlign="middle">
              <Icon sx={{ color: ({ palette: { success } }) => success.main }}>trending_up</Icon>
            </MDTypography>
            &nbsp;
            <MDTypography variant="button" color="text" fontWeight="medium">
              15%
            </MDTypography>{" "}
            전일 대비 참여도 상승
          </MDTypography>
        </MDBox>
      </MDBox>
      <MDBox p={2}>
        <TimelineItem
          color="info"
          icon="chat"
          title="id_kim: '신메뉴 가격이 얼마인가요?'"
          dateTime="방금 전"
        />
        <TimelineItem
          color="error"
          icon="favorite"
          title="user_star님이 '주말 이벤트' 게시물을 좋아합니다."
          dateTime="10분 전"
        />
        <TimelineItem
          color="success"
          icon="share"
          title="게시물이 카카오톡으로 12회 공유되었습니다."
          dateTime="35분 전"
        />
        <TimelineItem
          color="info"
          icon="chat"
          title="shop_boss: '디자인이 너무 예뻐요!'"
          dateTime="1시간 전"
        />
        <TimelineItem
          color="warning"
          icon="stars"
          title="새로운 팔로워 5명이 추가되었습니다."
          dateTime="3시간 전"
          lastItem
        />
      </MDBox>
    </Card>
  );
}

export default LiveFeedback;
