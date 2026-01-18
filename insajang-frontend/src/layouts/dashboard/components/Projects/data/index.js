/* eslint-disable react/prop-types */
/* eslint-disable react/function-component-definition */

// @mui material components
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";
import MDAvatar from "components/MDAvatar";
import MDProgress from "components/MDProgress";
import MDBadge from "components/MDBadge";

// 파일 경로 에러 방지용
import logoInstagram from "assets/images/small-logos/logo-xd.svg";

export default function data() {
  const PostInfo = ({ image, name, date }) => (
    <MDBox display="flex" alignItems="center" lineHeight={1}>
      <MDAvatar src={image} name={name} size="sm" variant="rounded" />
      <MDBox ml={2} lineHeight={1}>
        <MDTypography display="block" variant="button" fontWeight="medium">
          {name}
        </MDTypography>
        <MDTypography variant="caption">{date}</MDTypography>
      </MDBox>
    </MDBox>
  );

  return {
    columns: [
      { Header: "게시물", accessor: "post", width: "40%", align: "left" },
      { Header: "상태", accessor: "status", align: "center" },
      { Header: "반응도(좋아요)", accessor: "engagement", align: "center" },
      { Header: "진행률", accessor: "completion", align: "center" },
    ],

    rows: [
      {
        post: <PostInfo image={logoInstagram} name="신메뉴 출시 홍보물" date="2026-01-18" />,
        status: (
          <MDBox ml={-1}>
            <MDBadge badgeContent="업로드 완료" color="success" variant="gradient" size="sm" />
          </MDBox>
        ),
        engagement: (
          <MDTypography variant="caption" color="text" fontWeight="medium">
            1,204
          </MDTypography>
        ),
        completion: (
          <MDBox width="8rem" textAlign="left">
            <MDTypography variant="caption" color="text" fontWeight="medium">
              도달률 80%
            </MDTypography>
            <MDProgress value={80} color="info" variant="gradient" label={false} />
          </MDBox>
        ),
      },
      {
        post: <PostInfo image={logoInstagram} name="주말 이벤트 안내" date="2026-01-19" />,
        status: (
          <MDBox ml={-1}>
            <MDBadge badgeContent="예약 대기" color="info" variant="gradient" size="sm" />
          </MDBox>
        ),
        engagement: (
          <MDTypography variant="caption" color="text" fontWeight="medium">
            -
          </MDTypography>
        ),
        completion: (
          <MDBox width="8rem" textAlign="left">
            <MDTypography variant="caption" color="text" fontWeight="medium">
              대기중
            </MDTypography>
            <MDProgress value={0} color="secondary" variant="gradient" label={false} />
          </MDBox>
        ),
      },
    ],
  };
}
