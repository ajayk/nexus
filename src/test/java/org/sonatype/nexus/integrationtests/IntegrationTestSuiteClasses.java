/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.nexus.integrationtests.client.nexus758.Nexus758StatusService;
import org.sonatype.nexus.integrationtests.nexus1022.Nexus1022RebuildRepositoryMavenMetadataTaskTest;
import org.sonatype.nexus.integrationtests.nexus1197.Nexus1197CheckUserAgentTest;
import org.sonatype.nexus.integrationtests.nexus1239.Nexus1239PlexusUserResourceTest;
import org.sonatype.nexus.integrationtests.nexus1239.Nexus1239UserSearchTest;
import org.sonatype.nexus.integrationtests.nexus1286.Nexus1286RoleListTest;
import org.sonatype.nexus.integrationtests.nexus1329.Nexus1329ChecksumTest;
import org.sonatype.nexus.integrationtests.nexus1329.Nexus1329MirrorFailAndRetriesTest;
import org.sonatype.nexus.integrationtests.nexus1329.Nexus1329MirrorFailNoRetiesTest;
import org.sonatype.nexus.integrationtests.nexus1329.Nexus1329MirrorOnlyTest;
import org.sonatype.nexus.integrationtests.nexus1329.Nexus1329RetryMirrorTest;
import org.sonatype.nexus.integrationtests.nexus1329.Nexus1329UnavailableTest;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetValidationTests;
import org.sonatype.nexus.integrationtests.nexus1375.Nexus1375LogConfigTest;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserValidationTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesValidationTests;
import org.sonatype.nexus.integrationtests.nexus166.Nexus166SampleTest;
import org.sonatype.nexus.integrationtests.nexus167.Nexus167ReleaseToSnapshotTest;
import org.sonatype.nexus.integrationtests.nexus168.Nexus168SnapshotToReleaseTest;
import org.sonatype.nexus.integrationtests.nexus169.Nexus169ReleaseMetaDataInSnapshotRepoTest;
import org.sonatype.nexus.integrationtests.nexus1696.Nexus1696ValidateBaseUrl;
import org.sonatype.nexus.integrationtests.nexus176.Nexus176DeployToInvalidRepoTest;
import org.sonatype.nexus.integrationtests.nexus233.Nexus233PrivilegesCrudXMLTests;
import org.sonatype.nexus.integrationtests.nexus233.Nexus233PrivilegesValidationTests;
import org.sonatype.nexus.integrationtests.nexus258.Nexus258ReleaseDeployTest;
import org.sonatype.nexus.integrationtests.nexus259.Nexus259SnapshotDeployTest;
import org.sonatype.nexus.integrationtests.nexus260.Nexus260MultipleDeployTest;
import org.sonatype.nexus.integrationtests.nexus261.Nexus261NexusGroupDownloadTest;
import org.sonatype.nexus.integrationtests.nexus292.Nexus292SoftRestartTest;
import org.sonatype.nexus.integrationtests.nexus379.Nexus379VirtualRepoSameId;
import org.sonatype.nexus.integrationtests.nexus384.Nexus384DotAndDashSearchTest;
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesValidationTests;
import org.sonatype.nexus.integrationtests.nexus387.Nexus387RoutesTests;
import org.sonatype.nexus.integrationtests.nexus393.Nexus393ResetPasswordTest;
import org.sonatype.nexus.integrationtests.nexus394.Nexus394ForgotPasswordTest;
import org.sonatype.nexus.integrationtests.nexus395.Nexus395ForgotUsernameTest;
import org.sonatype.nexus.integrationtests.nexus408.Nexus408ChangePasswordTest;
import org.sonatype.nexus.integrationtests.nexus448.Nexus448PrivilegeURLTest;
import org.sonatype.nexus.integrationtests.nexus526.Nexus526FeedsTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudValidationTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudXMLTests;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupsCrudValidationTests;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupsCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskCronTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskManualTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskMonthlyTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskOnceTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskWeeklyTest;
import org.sonatype.nexus.integrationtests.nexus570.Nexus570IndexArchetypeTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586AnonymousChangePasswordTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586AnonymousForgotPasswordTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586AnonymousForgotUserIdTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586AnonymousResetPasswordTest;
import org.sonatype.nexus.integrationtests.nexus598.Nexus598ClassnameSearchTest;
import org.sonatype.nexus.integrationtests.nexus602.Nexus602SearchSnapshotArtifactTest;
import org.sonatype.nexus.integrationtests.nexus606.Nexus606DownloadLogsAndConfigFilesTest;
import org.sonatype.nexus.integrationtests.nexus634.Nexus634KeepNewSnapshotsTest;
import org.sonatype.nexus.integrationtests.nexus634.Nexus634KeepTwoSnapshotsTest;
import org.sonatype.nexus.integrationtests.nexus634.Nexus634RemoveAllTest;
import org.sonatype.nexus.integrationtests.nexus636.Nexus636EvictUnusedProxiedTaskTest;
import org.sonatype.nexus.integrationtests.nexus637.Nexus637PublishIndexTest;
import org.sonatype.nexus.integrationtests.nexus639.Nexus639PurgeTaskTest;
import org.sonatype.nexus.integrationtests.nexus640.Nexus640RebuildRepositoryAttributesTaskTest;
import org.sonatype.nexus.integrationtests.nexus642.Nexus642SynchShadowTaskTest;
import org.sonatype.nexus.integrationtests.nexus643.Nexus643EmptyTrashTaskTest;
import org.sonatype.nexus.integrationtests.nexus688.Nexus688ReindexOnRepoAdd;
import org.sonatype.nexus.integrationtests.nexus779.Nexus779DeployRssTest;
import org.sonatype.nexus.integrationtests.nexus782.Nexus782UploadWithClassifier;
import org.sonatype.nexus.integrationtests.nexus810.Nexus810PackageNamesInNexusConf;
import org.sonatype.nexus.integrationtests.nexus810.Nexus810PackageNamesInRestMessages;
import org.sonatype.nexus.integrationtests.nexus947.Nexus947GroupBrowsing;
import org.sonatype.nexus.integrationtests.nexus950.Nexus950CorruptPomTest;
import org.sonatype.nexus.integrationtests.nexus969.Nexus969CacheEvictInteractionTest;
import org.sonatype.nexus.integrationtests.nexus970.Nexus970DeleteRepositoryTest;
import org.sonatype.nexus.integrationtests.nexus980.Nexus980ReindexVirtualReposTest;
import org.sonatype.nexus.integrationtests.proxy.nexus1111.Nexus1111ProxyRemote500ErrorTest;
import org.sonatype.nexus.integrationtests.proxy.nexus177.Nexus177OutOfServiceTest;
import org.sonatype.nexus.integrationtests.proxy.nexus178.Nexus178BlockProxyDownloadTest;
import org.sonatype.nexus.integrationtests.proxy.nexus179.Nexus179RemoteRepoDownTest;
import org.sonatype.nexus.integrationtests.proxy.nexus262.Nexus262SimpleProxyTest;
import org.sonatype.nexus.integrationtests.proxy.nexus635.Nexus635ClearCacheTaskTest;
import org.sonatype.nexus.integrationtests.upgrades.nexus652.Nexus652Beta5To10UpgradeTest;
import org.sonatype.nexus.integrationtests.webproxy.nexus1101.Nexus1101NexusOverWebproxyTest;
import org.sonatype.nexus.integrationtests.webproxy.nexus1113.Nexus1113WebProxyWithAuthenticationTest;
import org.sonatype.nexus.integrationtests.webproxy.nexus1116.Nexus1116InvalidProxyTest;

/**
 *
 */
@RunWith( Suite.class )
@SuiteClasses( {
    Nexus166SampleTest.class,
    Nexus758StatusService.class,
    Nexus169ReleaseMetaDataInSnapshotRepoTest.class,
    Nexus258ReleaseDeployTest.class,
    Nexus167ReleaseToSnapshotTest.class,
    Nexus168SnapshotToReleaseTest.class,
    Nexus176DeployToInvalidRepoTest.class,
    Nexus259SnapshotDeployTest.class,
    Nexus260MultipleDeployTest.class,
    Nexus261NexusGroupDownloadTest.class,
    Nexus177OutOfServiceTest.class,
    Nexus178BlockProxyDownloadTest.class,
    Nexus179RemoteRepoDownTest.class,
    Nexus262SimpleProxyTest.class,
    Nexus292SoftRestartTest.class,
    Nexus133TargetCrudJsonTests.class,
    Nexus133TargetCrudXmlTests.class,
    Nexus142UserCrudJsonTests.class,
    Nexus142UserCrudXmlTests.class,
    Nexus156RolesCrudJsonTests.class,
    Nexus156RolesCrudXmlTests.class,
    Nexus142UserValidationTests.class,
    Nexus156RolesValidationTests.class,
    Nexus133TargetValidationTests.class,
    Nexus233PrivilegesCrudXMLTests.class,
    Nexus233PrivilegesValidationTests.class,
    Nexus393ResetPasswordTest.class,
    Nexus394ForgotPasswordTest.class,
    Nexus385RoutesCrudXmlTests.class,
    Nexus385RoutesValidationTests.class,
    Nexus387RoutesTests.class,
    Nexus395ForgotUsernameTest.class,
    Nexus408ChangePasswordTest.class,
    Nexus526FeedsTests.class,
    Nexus531RepositoryCrudXMLTests.class,
    Nexus531RepositoryCrudJsonTests.class,
    Nexus533TaskManualTest.class,
    Nexus533TaskOnceTest.class,
    Nexus533TaskWeeklyTest.class,
    Nexus533TaskMonthlyTest.class,
    Nexus533TaskCronTest.class,
    Nexus533TaskCronTest.class,
    Nexus233PrivilegesCrudXMLTests.class,
    Nexus379VirtualRepoSameId.class,
    Nexus448PrivilegeURLTest.class,
    Nexus586AnonymousChangePasswordTest.class,
    Nexus586AnonymousForgotPasswordTest.class,
    Nexus586AnonymousForgotUserIdTest.class,
    Nexus586AnonymousResetPasswordTest.class,
    Nexus532GroupsCrudXmlTests.class,
    Nexus532GroupsCrudValidationTests.class,
    Nexus606DownloadLogsAndConfigFilesTest.class,
    Nexus643EmptyTrashTaskTest.class,
    Nexus637PublishIndexTest.class,
    Nexus652Beta5To10UpgradeTest.class,
    Nexus602SearchSnapshotArtifactTest.class,
    Nexus635ClearCacheTaskTest.class,
    Nexus634RemoveAllTest.class,
    Nexus634KeepNewSnapshotsTest.class,
    Nexus634KeepTwoSnapshotsTest.class,
    Nexus636EvictUnusedProxiedTaskTest.class,
    Nexus639PurgeTaskTest.class,
    Nexus598ClassnameSearchTest.class,
    Nexus640RebuildRepositoryAttributesTaskTest.class,
    Nexus531RepositoryCrudValidationTests.class,
    Nexus810PackageNamesInRestMessages.class,
    Nexus810PackageNamesInNexusConf.class,
    Nexus782UploadWithClassifier.class,
    Nexus688ReindexOnRepoAdd.class,
    Nexus384DotAndDashSearchTest.class,
    Nexus642SynchShadowTaskTest.class,
    Nexus947GroupBrowsing.class,
    Nexus570IndexArchetypeTest.class,
    Nexus970DeleteRepositoryTest.class,
    Nexus969CacheEvictInteractionTest.class,
    Nexus980ReindexVirtualReposTest.class,
    Nexus950CorruptPomTest.class,
    Nexus779DeployRssTest.class,
    Nexus639PurgeTaskTest.class,
    Nexus1022RebuildRepositoryMavenMetadataTaskTest.class,
    Nexus1101NexusOverWebproxyTest.class,
    Nexus1113WebProxyWithAuthenticationTest.class,
    Nexus1116InvalidProxyTest.class,
    Nexus1111ProxyRemote500ErrorTest.class,
    Nexus1197CheckUserAgentTest.class,
    Nexus1239PlexusUserResourceTest.class,
    Nexus1239UserSearchTest.class,
    Nexus1286RoleListTest.class,
    Nexus1375LogConfigTest.class,
    Nexus1329ChecksumTest.class,
    Nexus1329MirrorFailAndRetriesTest.class,
    Nexus1329MirrorFailNoRetiesTest.class,
    Nexus1329MirrorOnlyTest.class,
    Nexus1329RetryMirrorTest.class,
    Nexus1329UnavailableTest.class,
    Nexus1696ValidateBaseUrl.class
} )
public class IntegrationTestSuiteClasses
{

}
